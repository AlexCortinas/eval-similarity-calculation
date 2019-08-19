package org.librairy.eval.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.librairy.eval.model.Neighbour;
import org.librairy.eval.model.Neighbourhood;
import org.librairy.eval.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PivotAlgorithm2 implements ClustererAlgorithm {
    private static int numAlg = 1;

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(PivotAlgorithm2.class);

    private final String id;
    private final int numPivots;
    private List<Point> pivots;
    private final List<Point> points;
    private final Map<String, Map<String, Double>> distancePivotToPoint = new HashMap<>();
    private double startRatio;

    private BiFunction<List<Double>, List<Double>, Double> comparison;

    public PivotAlgorithm2(int numPivots, BiFunction<List<Double>, List<Double>, Double> comparison) {
        this.id = "Pivot-based[" + numAlg++ + "]";
        this.numPivots = numPivots;
        this.pivots = new ArrayList<Point>();
        this.points = new ArrayList<Point>();
        this.comparison = comparison;
    }

    @Override
    public void add(Point point) {
        if (point != null && point.getId() != null && point.getVector() != null) {
            this.points.add(new Point(point));
        }
    }

    public List<Point> getPoints() {
        return this.points.stream().filter(p -> p != null).collect(Collectors.toList());
    }

//	ClustererReport cluster(): agrupa los puntos del dataset y devuelve un informe resultado de la operación. 
//   	En este método es donde se podrían inicializar los N pivotes, ya que se dispone del dataset completo. 
//		Una posible optimización de rendimiento sería hacerlo de forma incremental en el método 'add'según se 
//		vayan añadiendo puntos, pero creo que es mejor idea hacerlo desde aquí por si se define otra lógica
//		distinta a la asignación de los N primeros puntos como pivotes.

    @Override
    public ClustererReport cluster() {
        try {

            int randomIndex;
            while (this.pivots.size() < this.numPivots) {
                randomIndex = (int) (Math.random() * this.points.size());
                this.pivots.add(this.points.get(randomIndex));
            }
            try {
                for (Point pivot : this.pivots) {
                    Map<String, Double> pivotDistances = new HashMap<>();
                    for (Point point : this.getPoints()) {
                        try {
                            pivotDistances.put(point.getId(), comparison.apply(pivot.getVector(), point.getVector()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    this.distancePivotToPoint.put(pivot.getId(), pivotDistances);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            double min = 1000;
            double max = -1;
            for (Point p : this.pivots) {
                for (Double d : this.distancePivotToPoint.get(p.getId()).values()) {
                    min = Math.min(min, d);
                    max = Math.max(max, d);
                }
            }

            startRatio = min == 0.0 ? 0.1 : min;
            LOG.info("Min score: " + min);
            LOG.info("Max score: " + max);

            min = 1000;
            max = -1;
            double d;

            for (Point p : this.getPoints()) {
                for (Point u : this.getPoints()) {
                    if (!p.equals(u)) {
                        try {
                            d = comparison.apply(p.getVector(), u.getVector());
                            min = Math.min(min, d);
                            max = Math.max(max, d);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            LOG.info("Min abs score: " + min);
            LOG.info("Max abs score: " + max);

            ClustererReport report = new ClustererReport();
            report.setNumClusters(Long.valueOf(this.numPivots));
            double comparisons = this.points.size() * this.pivots.size();
            report.setNumComparisons((long) comparisons);
            return report;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//	Neighbourhood getNeighbourhood(Point point, Integer n): identifica los n puntos más cercanos a uno dado (point)
//	En este método es donde tendrías que comparar el punto dado 'point' con los pivotes que finalmente se han definido en el dataset

    @Override
    public Neighbourhood getNeighbourhood(Point point, Integer size) {
        LOG.info("Creating neighbourhood around " + point + " with " + size + " neighbours");

        List<Neighbour> neighbours = pivots.parallelStream()
                .map(neighbourPoint -> new Neighbour(neighbourPoint,
                        comparison.apply(point.getVector(), neighbourPoint.getVector())))
                .filter(a -> a != null).collect(Collectors.toList());

        double multiplier = 1.2;
        double ratio = startRatio * multiplier;
        List<Neighbour> found;
        while ((found = rangeQuery(point, ratio, neighbours)).size() < size) {
            LOG.info("Found: " + found.size());
            ratio = ratio * multiplier;
        }

        List<Neighbour> topNeighbours = neighbours.parallelStream()
                .sorted((a, b) -> -a.getScore().compareTo(b.getScore())).limit(size).collect(Collectors.toList());

        Neighbourhood neighbourhood = new Neighbourhood();
        neighbourhood.setNumberOfNeighbours(neighbours.size());
        neighbourhood.setReference(point);
        neighbourhood.setClosestNeighbours(topNeighbours);
        return neighbourhood;

    }

    private Neighbour find(Point point, List<Neighbour> neighbours) {
        return neighbours.stream().filter(n -> n.getPoint().equals(point)).findFirst().orElse(null);
    }

    private List<Neighbour> rangeQuery(Point query, double ratio, final List<Neighbour> neighbours) {
        for (Point pivot : this.pivots) {
            Neighbour pivotAsNeighbour = find(pivot, neighbours);
            if (pivotAsNeighbour == null) {
                LOG.error("Los pivotes están siempre!");
            }
            for (Point possibleNeighbour : this.points) {
                if (Math.abs(this.distancePivotToPoint.get(pivot.getId()).get(possibleNeighbour.getId())
                        - pivotAsNeighbour.getScore()) <= ratio) {
                    if (find(possibleNeighbour, neighbours) == null) {
                        // si no existe hay que añadirlo a la lista de posibles
                        neighbours.add(new Neighbour(possibleNeighbour,
                                comparison.apply(query.getVector(), possibleNeighbour.getVector())));
//                                JensenShannon.similarity(query.getVector(), possibleNeighbour.getVector())));
                    }
                } else {
//                    LOG.info("Saltado");
                }
            }
        }

        return neighbours.stream().filter(neighbour -> neighbour.getScore() <= ratio).collect(Collectors.toList());
    }

//	String getCluster(Point point): devuelve el identificador (String) del cluster al que pertenece un punto
//	Una vez se han identificado los grupos de puntos en el metodo 'cluster', se asignan identificadores a 
//	cada uno de esos grupos. Con este método se devuelve el identificador del grupo al que pertence el 
//	punto dado 'point'.
    @Override
    public String getCluster(Point point) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public String toString() {
        return getId() + " Algorithm";
    }

}
