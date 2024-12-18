package com.fulldev.spectator.principal;

import com.fulldev.spectator.model.*;
import com.fulldev.spectator.repository.SerieRepository;
import com.fulldev.spectator.service.ConsumoAPI;
import com.fulldev.spectator.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=a02a07da";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;


    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }


    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1- Buscar series.
                    2- Buscar episodios.
                    3- Mostrar series buscadas.
                    4- Buscar series por título.
                    5- Top 5 mejores series.
                    6- Buscar series por categoría.
                    7- Filtrar series por temporadas y evaluación.
                    8- Buscar episodios por título.
                    9- Top 5 episodios por serie.
                    
                    0- Salir.""";
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    busquedaSeries();
                    break;
                case 2:
                    busquedaEpisodios();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    busquedaSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadasYEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Hasta luego!");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas consultar: ");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos.sinopsis());
        return datos;
    }

    private void busquedaEpisodios() {
        mostrarSeriesBuscadas();
        System.out.println("Ingresa el nombre de la serie que quieres ver: ");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieBuscada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieBuscada.getTotalDeTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieBuscada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieBuscada.setEpisodios(episodios);
            repositorio.save(serieBuscada);
        }
    }


    private void busquedaSeries() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        System.out.println(datos);
    }


    private void mostrarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void busquedaSeriesPorTitulo() {
        System.out.println("Ingresa el nombre de la serie que deseas buscar: ");
        var nombreSerie = teclado.nextLine();
        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("La serie es: " + serieBuscada.get());
        } else {
            System.out.println("Serie no disponible.");
        }
    }

    private void buscarTop5Series() {
        List<Serie> top5Series = repositorio.findTop5ByOrderByEvaluacionDesc();
        top5Series.forEach(s -> System.out.println("Serie; " + s.getTitulo() + "; Evaluación: " + s.getEvaluacion()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Ingresa el género/categoría de las series que deseas consultar: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series de la categoría" + genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void filtrarSeriesPorTemporadasYEvaluacion() {
        System.out.println("Filtrar series a partir de cuantas temporadas: ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Filtrar series a partir de cual evaluación: ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYEvaluacion(totalTemporadas, evaluacion);
        System.out.println("*** Series Filtradas ***");
        filtroSeries.forEach(s -> System.out.println("título: " + s.getTitulo() + ", total temporadas: " + s.getTotalDeTemporadas() + " - evaluación: " + s.getEvaluacion()));

    }

    private void buscarEpisodiosPorTitulo() {
        System.out.println("Ingresa el nombre del episodio que deseas buscar: ");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s\n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));
    }

    private void buscarTop5Episodios() {
        busquedaSeriesPorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            List<Episodio> top5Episodios = repositorio.top5Episodios(serie);
            top5Episodios.forEach(e -> System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s\n",
               e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));
        }
    }
}
