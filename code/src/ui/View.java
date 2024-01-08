package ui;

import java.time.LocalDateTime;
import java.util.List;

public class View {

    public void println(String str) {
        System.out.println(str);
    }

    public void menuInicial() {
        this.println("");
        this.println("---------------------------------");
        this.println("  1. Adicionar novo pedido");
        this.println("  2. Adicionar pedido pendente");
        this.println("  3. Iniciar turno");
        this.println("  4. Listar notificações");
        this.println("  0. Exit");
        this.println("---------------------------------");
    }

    public void menuPedido() {
        this.println("");
        this.println("---------------------------------");
        this.println("  1. Adicionar serviço");
        this.println("  2. Concluir pedido");
        this.println("  0. Exit");
        this.println("---------------------------------");
    }

    public void semServicosAdicionados()    { this.println("Não foram adicionados serviços"); }
    public void erro()                      { this.println("Erro. Tente outra vez"); }
    public void estimativa(LocalDateTime e) { this.println("Estimativa: " + e); }
    public void concluir()                  { this.println("Concluir? [s/n]"); }
    public void notificar()                 { this.println("Notificar cliente? [s/n]"); }
    public void pedidoAdicionadoSucesso()   { this.println("Pedido adicionado com sucesso"); }
    public void erroAdicionarPedido()       { this.println("Erro ao adicionar pedido"); }
    public void semPedidosPendentes()       { this.println("A ficha do veículo não possui pedidos pendentes"); }
    public void escolherPedido()            { this.println("Escolha o pedido: (0 - sair)"); }

    public void menuTipoServico() {
        this.println("");
        this.println("---------------------------------");
        this.println("  1. Universal");
        this.println("  2. Motor eletrico");
        this.println("  3. Motor a combustao");
        this.println("  4. Motor a gasolina");
        this.println("  5. Motor a diesel");
        this.println("  0. Exit");
    }

    public void estimativaPrompt()          { this.println("Insira a estimativa de duração do serviço em minutos:"); }
    public void nifPrompt()                 { this.println("Insira o NIF do cliente:"); }
    public void matriculaPrompt()           { this.println("Insira a matricula do veiculo:");}

    public void menuPostoTrabalho() {
        this.println("");
        this.println("---------------------------------");
        this.println("  1. Listar serviços");
        this.println("  2. Iniciar serviço");
        this.println("  3. Gerar serviço");
        this.println("  4. Terminar serviço");
        this.println("  0. Terminar turno");
        this.println("---------------------------------");
    }

    public void nrMecanicoPrompt()          { this.println("Insira o número do mecânico:"); }
    public void nrPostoPrompt()             { this.println("Insira o número do posto de trabalho:"); }
    public void postoNaoExiste()            { this.println("Posto de trabalho não existe."); }
    public void erroTerminarTurno()         { this.println("Não pode terminar o turno com um serviço por concluir"); }
    public void estadoConclusao()           { this.println("Concluido com sucesso? [s/n]"); }
    public void motivoPrompt()              { this.println("Insira o motivo:"); }


    public void printList(List<String> list) {
        StringBuilder str = new StringBuilder();
        int count = 1;
        for (String jobName : list) {
            str.append(count).append(". ").append(jobName).append("\n");
            count++;
        }
        this.println(str.toString());
    }
}
