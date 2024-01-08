package ui;

import business.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Controller {
    private final IGestorOficina gestorOficina;
    private final NomesServicos nomesServicos;
    private final Scanner scanner;
    private final View view;
    private int option;

    public Controller(Scanner scanner) {
        this.gestorOficina = GestorOficina.getInstance();
        this.nomesServicos = NomesServicos.getInstance();
        this.scanner = scanner;
        this.view = new View();
        this.option = -1;
    }

    public void start() {
        while (this.option != 0) {
            this.view.menuInicial();
            this.option = this.scanner.nextInt();
            switch (this.option) {
                case 1 -> this.pedidos();
                case 2 -> this.pedidosPendentes();
                case 3 -> this.postoTrabalho();
                case 4 -> this.notificacoes();
                case 0 -> this.view.println("Exiting...");
                default -> this.view.println("Invalid option!");
            }
        }
    }

    public Servico criarServico() {
        this.view.menuTipoServico();
        this.option = -1;
        while (this.option < 1 || this.option > TipoServico.values().length) {
            try {
                String op = this.scanner.next();
                this.option = Integer.parseInt(op);
            } catch (NumberFormatException e) {
                this.view.erro();
            }
        }

        TipoServico tipoServico = TipoServico.fromId(this.option);

        List<String> servicosDisponiveis = this.nomesServicos.getServicos(tipoServico);
        this.view.printList(servicosDisponiveis);

        do { this.option = this.scanner.nextInt(); }
        while (this.option < 1 || this.option > servicosDisponiveis.size());

        String desc = servicosDisponiveis.get(this.option - 1);

        this.view.estimativaPrompt();
        while (true) {
            try {
                String estimativa = this.scanner.next();
                int horas = Integer.parseInt(estimativa);
                return this.gestorOficina.criarServico(desc, tipoServico, horas);
            } catch (NumberFormatException e) {
                this.view.erro();
            }
        }
    }

    public boolean simOuNao() {
        boolean flag = false;
        while (true) {
            try {
                String c = this.scanner.next();
                c = c.toLowerCase();

                if (c.equals("s")) {
                    flag = true;
                    break;
                } else if (c.equals("n")) {
                    break;
                }
            } catch (Exception e) {
                this.view.erro();
            }
        }
        return flag;
    }

    public void pedidosPendentes() {
        String matricula = verificarCliente();
        boolean temPedidosPendentes = this.gestorOficina.verificarPedidosPendentes(matricula);
        if (!temPedidosPendentes) {
            this.view.semPedidosPendentes();
            return;
        }

        List<InfoPedido> pedidos = this.gestorOficina.getPedidosPendentes(matricula);
        this.view.printList(pedidos.stream().map(InfoPedido::toString).toList());

        this.view.escolherPedido();
        int nr = this.scanner.nextInt();
        if (nr == 0 || nr > pedidos.size()) {
            this.view.erro();
            return;
        }

        int pedidoID = pedidos.get(nr - 1).getId();

        this.view.notificar();
        boolean notificar = this.simOuNao();


        try {
            this.gestorOficina.addPedidoPendente(pedidoID, notificar, matricula);
            this.view.pedidoAdicionadoSucesso();
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            this.view.erroAdicionarPedido();
            this.view.println(e.getMessage());
        }
    }

    public void pedidos() {
        List<Servico> servicos = new ArrayList<>();
        String matricula = verificarCliente();

        while (this.option != 0) {

            this.view.menuPedido();

            while (true) {
                try {
                    String op = this.scanner.next();
                    this.option = Integer.parseInt(op);
                    break;
                } catch (NumberFormatException e) {
                    this.view.erro();
                }
            }

            switch (this.option) {
                case 1 -> {
                    Servico servico = this.criarServico();
                    servicos.add(servico);
                }
                case 2 -> {
                    if (servicos.isEmpty()) {
                        this.view.semServicosAdicionados();
                        break;
                    }

                    try {
                        LocalDateTime estimativa = this.gestorOficina.gerarEstimativa(servicos, matricula);
                        this.view.estimativa(estimativa);

                        this.view.concluir();
                        boolean concluir = this.simOuNao();

                        if (!concluir)
                            break;

                        this.view.notificar();
                        boolean notificar = this.simOuNao();

                        try {
                            this.gestorOficina.addPedido(matricula, servicos, notificar);
                            this.view.pedidoAdicionadoSucesso();
                        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
                            this.view.erroAdicionarPedido();
                            this.view.println(e.getMessage());
                            for (Servico servico : servicos) {
                                this.gestorOficina.removerServico(servico.getId());
                            }
                        }
                        this.option = 0;
                    } catch (IllegalStateException e) {
                        this.view.println(e.getMessage());
                    }
                }
                case 0 -> this.view.println("Exiting ...");
                default -> this.view.println("Opção inválida");
            }
        }
        this.option = -1;
    }

    private String verificarCliente() {
        String matricula;

        while (true) {
            this.view.nifPrompt();

            try {
                String nifStr = this.scanner.next();
                int nif = Integer.parseInt(nifStr);

                this.view.matriculaPrompt();
                matricula = this.scanner.next();

                if (this.gestorOficina.verificarVeiculoCliente(nif, matricula)) {
                    break;
                }

            } catch (NumberFormatException e) {
                this.view.erro();
            }
        }

        return matricula;
    }

    public void postoTrabalho() {

        this.view.nrMecanicoPrompt();
        int nrMecanico = this.scanner.nextInt();

        this.view.nrPostoPrompt();
        int nrPosto = this.scanner.nextInt();

        if (!this.gestorOficina.verificarPostoTrabalho(nrPosto)) {
            this.view.postoNaoExiste();
            return;
        }

        try {
            this.gestorOficina.iniciarTurno(nrPosto, nrMecanico);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            this.view.println(e.getMessage());
            return;
        }

        boolean comServico = false;

        while (this.option != 0) {
            this.view.menuPostoTrabalho();
            this.option = this.scanner.nextInt();
            switch (this.option) {
                case 1 -> {
                    List<InfoServico> servicos = this.gestorOficina.listarServicos(nrPosto);
                    this.view.printList(servicos.stream().map(InfoServico::toString).toList());
                }
                case 2 -> {
                    try {
                        this.gestorOficina.iniciarServico(nrPosto);
                        comServico = true;
                    } catch (IllegalStateException | NoSuchElementException e) {
                        this.view.println(e.getMessage());
                    }
                }
                case 3 -> {
                    Servico s = this.criarServico();
                    try {
                        this.gestorOficina.gerarServico(nrPosto, s);
                    } catch (IllegalStateException | NoSuchElementException e) {
                        this.view.println(e.getMessage());
                    }
                }
                case 4 -> {
                    comServico = false;
                    this.view.estadoConclusao();
                    boolean estado = this.simOuNao();

                    if (!estado) {
                        this.view.motivoPrompt();
                        String motivo = this.scanner.next();
                        try {
                            this.gestorOficina.terminarServico(nrPosto, motivo);
                        } catch (IllegalStateException | NoSuchElementException e) {
                            this.view.println(e.getMessage());
                        }
                        break;
                    } else {
                        try {
                            this.gestorOficina.terminarServico(nrPosto);
                            comServico = false;
                        } catch (IllegalStateException | NoSuchElementException e) {
                            this.view.println(e.getMessage());
                        }
                    }
                }
                case 0 -> {
                    if (comServico) {
                        this.option = -1;
                        this.view.erroTerminarTurno();
                        break;
                    }
                    try {
                        this.gestorOficina.terminarTurno(nrPosto);
                    } catch (IllegalStateException | NoSuchElementException e) {
                        this.view.println(e.getMessage());
                    }
                }
                default -> this.view.println("Invalid option!");
            }
        }
        this.option = -1;
    }

    private void notificacoes() {
        this.view.nifPrompt();
        try {
            String nifStr = this.scanner.next();
            int nif = Integer.parseInt(nifStr);

            try {
                List<String> notificacoes = this.gestorOficina.getNotificacoes(nif);
                this.view.printList(notificacoes);
            } catch (NoSuchElementException e) {
                this.view.println(e.getMessage());
            }

        } catch (NumberFormatException e) {
            this.view.erro();
        }
    }
}
