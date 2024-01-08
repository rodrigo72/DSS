package business;

import data.PedidoDAO;

import java.util.*;

public class FichaVeiculo {
    private final String matricula;
    private final int nif;
    private final TipoMotor tipoMotor;
    private final Set<Integer> pedidosIds;
    private final Map<Integer, Pedido> pedidos;

    public FichaVeiculo(String matricula, int nif, TipoMotor tipoMotor, Set<Integer> pedidos) {
        this.matricula = matricula;
        this.nif = nif;
        this.tipoMotor = tipoMotor;
        this.pedidos = PedidoDAO.getInstance();
        this.pedidosIds = new HashSet<>(pedidos);
    }

    public FichaVeiculo(FichaVeiculo f) {
        this.matricula = f.getMatricula();
        this.nif = f.getNif();
        this.tipoMotor = f.getTipoMotor();
        this.pedidos = PedidoDAO.getInstance();
        this.pedidosIds = f.getPedidosIds();
    }

    public boolean verificarPedidosPendentes()  {
        for (Integer id : this.pedidosIds) {
            Pedido p = this.pedidos.get(id);
            if (p.getEstado() == EstadoPedido.PENDENTE) {
                return true;
            }
        }
        return false;
    }
    public List<Pedido> getPedidosPendentes()   {
        List<Pedido> result = new ArrayList<>();
        for (Integer id : this.pedidosIds) {
            Pedido p = this.pedidos.get(id);
            if (p.getEstado() == EstadoPedido.PENDENTE) {
                result.add(p.clone());
            }
        }
        return result;
    }

    public String getMatricula()                { return matricula; }
    public int getNif()                         { return nif; }
    public TipoMotor getTipoMotor()             { return tipoMotor; }
    public Set<Integer> getPedidosIds()         { return new HashSet<>(pedidosIds); }
    public FichaVeiculo clone() { return new FichaVeiculo(this); }

}
