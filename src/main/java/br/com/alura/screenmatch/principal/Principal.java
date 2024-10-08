package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=e02a423e";

    public void exibeMenu(){
        System.out.println("Digite o nome d série para a busca: ");
        var nomeSerie = leitura.nextLine();

        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();


		for (int i = 1; i <= dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			//json = consumo.obterDados("https://www.omdbapi.com/?t=gilmore+girls&season=" + i + "&apikey=e02a423e");
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		//temporadas.forEach(System.out::println);

//        for(int i = 0; i <= dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++){
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        //usando função lambidas
        //temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

//        temporadas.forEach(t -> System.out.println(t));
//        pode ser substituida por
//        temporadas.forEach(System.out::println);

//        ----------------

//        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());
//
//        System.out.println("\n Top 10 episódios");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(e -> System.out.println("Ordenação " + e))
//                .limit(10)
//                .peek(e -> System.out.println("Limite " + e))
//                .map(e-> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Mapeamento " + e))
//                .forEach(System.out::println);

        //----------------------------

        //bucando espidoios por titulo

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()    //"junta" a tabela temporadas com séries
                .map(d -> new Episodio(t.numero(), d )) //traforma cada linha em um novo episodio
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);
//
//        System.out.println("Digite um trecho do título do espisódio: ");
//        var trechoTitulo = leitura.nextLine();
//        //Ele é como um contêiner, lembra uma lista. Ele é mais generico e me permite usar função para vê se tem algo la dentro ou não
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();//Garante que irá vir o primeiro episódio
//
//        if(episodioBuscado.isPresent()){
//            System.out.println("Episódio encontrado");
//            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//        }else{
//            System.out.println("Espisódio não encontrado!");
//        }

        //--------------------

//        System.out.println("A partir de que ano você deseja ver os espisódios?");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate databusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//            .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(databusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " Episódio: " + e.getTitulo() +
//                                " Data lançamento: " + e.getDataLancamento().format(formatador)
//                ));
//
        //----------------------------------------------------------
        //Agrupamento de dadoos usando dicionario (mapa: inteiro para temporada e double para avaliação)
        //Assosciasão da média de avaliação para cada temporada

        Map<Integer, Double> avaliacoesPorTemporadas = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                //Faz o agrupamento de dados
                .collect(Collectors.groupingBy(Episodio::getTemporada, //pega episodio pela temporada
                        Collectors.averagingDouble(Episodio::getAvaliacao))); //faz a media das valiações de cada temporada

        System.out.println(avaliacoesPorTemporadas);
        //{1=8.2875, 2=5.325, 3=0.0}

        //-----------------------------------------------------------
        //Classe do java que facilita a aprte de estartitisca na parte de copntagem, soma, divisão...

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade: " + est.getCount());

    }
}
