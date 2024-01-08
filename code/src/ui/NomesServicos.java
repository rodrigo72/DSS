package ui;

import business.TipoServico;

import java.util.List;
import java.util.Map;

public class NomesServicos {

    private static NomesServicos singleton = null;
    private Map<TipoServico, List<String>> map;

    public static NomesServicos getInstance() {
        if (NomesServicos.singleton == null) {
            NomesServicos.singleton = new NomesServicos();
        }
        return NomesServicos.singleton;
    }

    private NomesServicos() {
        List<String> servicosUniversais = List.of(
                "Check-up",
                "Substituição de pneus",
                "Calibragem das rodas",
                "Alinhamento da direção",
                "Substituição dos injetores",
                "Substituição dos calços dos travões",
                "Mudança do óleo dos travões",
                "Limpeza interior",
                "Limpeza exterior",
                "Substituição de filtro de ar");

        List<String> servicosEletrico = List.of(
                "Avalialão do desempenho da bateria",
                "Substituição da bateria"
        );

        List<String> servicosCombustao = List.of(
                "Mudança de óleo do motor",
                "Mudança dos filtros de óleo",
                "Mudança dos filtros de ar",
                "Mudança dos filtros de combustível",
                "Substituição do conversor catalítico",
                "Substituição da bateria de arranque"
        );

        List<String> servicosGasoleo = List.of(
                "Substituição das velas de incandescência",
                "Substituição do filtro de partículas"
        );

        List<String> servicosGasolina = List.of(
                "Substituição da válvula do acelerador",
                "Substituição das velas de ignição"
        );

        this.map = Map.of(
                TipoServico.UNIVERSAL, servicosUniversais,
                TipoServico.ELETRICO, servicosEletrico,
                TipoServico.COMBUSTAO, servicosCombustao,
                TipoServico.GASOLEO, servicosGasoleo,
                TipoServico.GASOLINA, servicosGasolina
        );
    }

    public List<String> getServicos(TipoServico tipoServico) {
        return List.copyOf(this.map.get(tipoServico));
    }
}
