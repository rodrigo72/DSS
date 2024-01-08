package business;

import data.MecanicoDAO;
import data.PostoTrabalhoDAO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class GestorPostosTrabalho implements IGestorPostosTrabalho {
    private static GestorPostosTrabalho singleton = null;
    private Map<TipoServico, List<PostoTrabalho>> listas;
    private Map<Integer, PostoTrabalho> postosTrabalho;
    private Map<Integer, Mecanico> mecanicos;

    private record Result(PostoTrabalho postoTrabalho, IntervaloTempo intervaloTempo) {}

    public static GestorPostosTrabalho getInstance() {
        if (GestorPostosTrabalho.singleton == null)
            GestorPostosTrabalho.singleton = new GestorPostosTrabalho();
        return GestorPostosTrabalho.singleton;
    }

    private GestorPostosTrabalho() {
        this.listas = new HashMap<>();
        this.postosTrabalho = new HashMap<>();
        this.mecanicos = MecanicoDAO.getInstance();
        this.criarMap();
        this.criarListas();
    }

    private void criarListas() {
        for (TipoServico tipoServico : TipoServico.values())
            this.listas.put(tipoServico, new ArrayList<>());

        for (PostoTrabalho postoTrabalho : this.postosTrabalho.values())
            this.listas.get(postoTrabalho.getTipoServico()).add(postoTrabalho);
    }

    private void criarMap() {
        this.postosTrabalho = new HashMap<>();
        for (PostoTrabalho postoTrabalho : PostoTrabalhoDAO.getInstance().values())
            this.postosTrabalho.put(postoTrabalho.getNr(), postoTrabalho);
    }

    public void addPostoTrabalho(PostoTrabalho postoTrabalho) {
        this.postosTrabalho.put(postoTrabalho.getNr(), postoTrabalho);
    }

    public void addMecanico(Mecanico mecanico) {
        this.mecanicos.put(mecanico.getId(), mecanico);
    }

    public void iniciarTurno(int nrPosto, int idMecanico)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException {

        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        Mecanico mecanico = this.mecanicos.get(idMecanico);

        if (postoTrabalho != null && mecanico != null) {
            postoTrabalho.iniciarTurno(mecanico);
        } else {
            throw new NoSuchElementException("Posto de trabalho ou mecânico não existem.");
        }
    }

    public void terminarTurno(int nrPosto) throws NoSuchElementException, IllegalStateException {
        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        if (postoTrabalho != null)
            postoTrabalho.terminarTurno();
        else {
            throw new NoSuchElementException("Posto de trabalho não existe.");
        }
    }

    public List<InfoServico> listarServicos(int nrPosto) throws NoSuchElementException {
        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        if (postoTrabalho != null) {
            return postoTrabalho.getServicos().stream().map(Servico::getInfoServico).collect(toList());
        } else {
            throw new NoSuchElementException("Posto de trabalho não existe.");
        }
    }

    public void addServico(Servico servico, List<Integer> idsPedidos) throws IllegalStateException, IllegalArgumentException {

        List<PostoTrabalho> pQueue = this.listas.get(servico.getTipoServico());

        if (pQueue == null || pQueue.isEmpty()) {
            throw new IllegalStateException("Não há postos de trabalho disponíveis para o serviço " + servico.getId());
        }

        Map<PostoTrabalho, List<IntervaloTempo>> horasLivres = this.horasLivres(pQueue);
        List<IntervaloTempo> ocupados = this.getOcupados(idsPedidos);

        Result r = this.escolher(servico, horasLivres, ocupados);

        if (r.postoTrabalho != null) {
            r.postoTrabalho.addServico(servico, r.intervaloTempo);
        } else {
            throw new IllegalStateException("Não há postos de trabalho disponíveis para o serviço " + servico.getId());
        }
    }

    private Result escolher(Servico servico, Map<PostoTrabalho, List<IntervaloTempo>> horasLivres,
                            List<IntervaloTempo> ocupados) throws IllegalStateException, IllegalArgumentException {

        PostoTrabalho postoEscolhido = null;
        IntervaloTempo intervaloTempoEscolhido = null;

        List<IntervaloTempo> validos = new ArrayList<>();
        Collections.sort(ocupados);
        LocalDateTime aux = LocalDateTime.now();

        for (IntervaloTempo i : ocupados) {
            if (Duration.between(aux, i.inicio()).toMinutes() >= servico.getEstimativaDuracaoMinutos())
                validos.add(new IntervaloTempo(aux, i.inicio()));
            aux = i.fim();
        }

        if (validos.isEmpty()) {
            validos.add(new IntervaloTempo(aux, null));
        }

        Collections.sort(validos);

        for (Map.Entry<PostoTrabalho, List<IntervaloTempo>> entry : horasLivres.entrySet()) {
            PostoTrabalho p = entry.getKey();
            List<IntervaloTempo> intervaloTemposLivres = entry.getValue();

            for (IntervaloTempo i : intervaloTemposLivres) {
                IntervaloTempo disponivel = this.getIntervaloDisponivel(i, validos, servico.getEstimativaDuracaoMinutos());
                if (disponivel != null) {
                    disponivel = new IntervaloTempo(disponivel.inicio(), disponivel.inicio().plusMinutes(servico.getEstimativaDuracaoMinutos()));
                    if (intervaloTempoEscolhido == null || intervaloTempoEscolhido.inicio().isAfter(disponivel.inicio())) {
                        intervaloTempoEscolhido = disponivel;
                        postoEscolhido = p;
                    }
                }
            }
        }

        return new Result(postoEscolhido, intervaloTempoEscolhido);
    }

    private IntervaloTempo getIntervaloDisponivel(IntervaloTempo livre, List<IntervaloTempo> validos, int duracao) {
        LocalTime inicioL = (livre.inicio().toLocalTime()).withSecond(0).withNano(0);
        LocalTime fimL = (livre.fim() == null ? LocalTime.MAX : livre.fim().toLocalTime()).withSecond(0).withNano(0);
        LocalDate data = livre.inicio().toLocalDate();

        for (IntervaloTempo valido : validos) {
            LocalTime inicioV = (valido.inicio().toLocalTime()).withSecond(0).withNano(0);
            LocalTime fimV = (valido.fim() == null ? LocalTime.MAX : valido.fim().toLocalTime()).withSecond(0).withNano(0);

            if (fimL.isBefore(inicioV) || fimV.isBefore(inicioL))
                continue;

            LocalTime inicio, fim;
            if (inicioL.isBefore(inicioV)) inicio = inicioV;
            else inicio = inicioL;

            if (fimL.isAfter(fimV)) fim = fimV;
            else fim = fimL;

            if (Duration.between(inicio, fim).toMinutes() >= duracao)
                return new IntervaloTempo(inicio.atDate(data), fim.atDate(data));
        }

        return null;
    }

    public boolean removerServico(int id) {
        for (PostoTrabalho postoTrabalho : this.postosTrabalho.values()) {
            return postoTrabalho.removerServico(id);
        }
        return false;
    }

    public void gerarServico(int nrPosto, Servico s) throws NoSuchElementException, IllegalStateException {
        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        if (postoTrabalho != null) {
            postoTrabalho.gerarServico(s);
        } else {
            throw new NoSuchElementException("Posto de trabalho não existe.");
        }
    }

    public void iniciarServico(int nrPosto) throws NoSuchElementException, IllegalStateException {
        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        if (postoTrabalho != null) {
            postoTrabalho.iniciarServico();
            this.postosTrabalho.put(nrPosto, postoTrabalho);
        } else {
            throw new NoSuchElementException("Posto de trabalho não existe.");
        }
    }

    public Servico terminarServico(int nrPosto) throws NoSuchElementException, IllegalStateException {
        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        if (postoTrabalho != null) {
            Servico s = postoTrabalho.terminarServico();
            this.postosTrabalho.put(nrPosto, postoTrabalho);
            return s;
        } else {
            throw new NoSuchElementException("Posto de trabalho não existe.");
        }
    }

    public Servico terminarServico(int nrPosto, String motivo) throws NoSuchElementException, IllegalStateException {
        PostoTrabalho postoTrabalho = this.postosTrabalho.get(nrPosto);
        if (postoTrabalho != null) {
            return postoTrabalho.terminarServico(motivo);
        } else {
            throw new NoSuchElementException("Posto de trabalho não existe.");
        }
    }

    private List<IntervaloTempo> getOcupados(List<Integer> idsPedidos) {
        List<IntervaloTempo> ocupados = new ArrayList<>();
        for (PostoTrabalho p : this.postosTrabalho.values()) {
            ocupados.addAll(p.getIntervalosTempoOcupado(idsPedidos));
        }
        return ocupados;
    }

    private Map<PostoTrabalho, List<IntervaloTempo>> horasLivres(List<PostoTrabalho> pQueue) {
        Map<PostoTrabalho, List<IntervaloTempo>> horasLivres = new HashMap<>();
        for (PostoTrabalho postoTrabalho : pQueue) {
            horasLivres.put(postoTrabalho, postoTrabalho.getIntervalosTempoLivres()
                    .stream()
                    .filter(i -> i.inicio() != null)
                    .toList());
        }
        return horasLivres;
    }

    public LocalDateTime gerarEstimativa(List<Servico> servicos, List<Integer> idsPedidos) throws IllegalStateException {
        LocalDateTime estimativa = LocalDateTime.now();

        if (!servicos.isEmpty()) {
            Servico s = servicos.get(0);
            List<PostoTrabalho> pQueue = this.listas.get(s.getTipoServico());

            if (pQueue == null || pQueue.isEmpty()) {
                throw new IllegalStateException("Não há postos de trabalho disponíveis para o serviço " + s.getId());
            }

            Map<PostoTrabalho, List<IntervaloTempo>> horasLivres = this.horasLivres(pQueue);
            List<IntervaloTempo> ocupados = this.getOcupados(idsPedidos);

            for (Servico servico : servicos) {
                Result r = this.escolher(servico, horasLivres, ocupados);
                if (r.postoTrabalho == null || r.intervaloTempo == null) {
                    throw new IllegalStateException("Não há postos de trabalho disponíveis para o serviço " + servico.getId());
                }
                if (estimativa.isBefore(r.intervaloTempo.fim()))
                    estimativa = r.intervaloTempo.fim();

                ocupados.add(r.intervaloTempo);
            }

        }

        return estimativa;
    }

    public boolean verificarPostoTrabalho(int nrPosto) {
        return this.postosTrabalho.containsKey(nrPosto);
    }
}