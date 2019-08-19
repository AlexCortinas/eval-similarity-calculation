package org.librairy.eval.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.librairy.eval.metrics.JensenShannon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Neighbourhood {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(Neighbourhood.class);

    private Point reference;

    private List<Neighbour> closestNeighbours;

    // Store all the neighbours compared in order to count the number of comparisons
    private Integer numberOfNeighbours;

    private Double minScore;

    public Neighbourhood() {
        this.minScore = 0.0;
    }

    public Neighbourhood(Point reference, List<Neighbour> closestNeighbours) {
        this.reference = reference;
        this.closestNeighbours = closestNeighbours;
        this.numberOfNeighbours = closestNeighbours.size();
        this.minScore = closestNeighbours.stream().reduce((a,b) -> (a.getScore() >= b.getScore())? b : a).get().score;
    }

    public Neighbourhood(Point reference, Integer numberOfNeighbours) {
        this.reference = reference;
        this.closestNeighbours = new ArrayList<>();
        this.numberOfNeighbours = numberOfNeighbours;
        this.minScore = 0.0;
    }

    public Point getReference() {
        return reference;
    }

    public void setReference(Point reference) {
        this.reference = reference;
    }

    public List<Neighbour> getClosestNeighbours() {
        return closestNeighbours;
    }

    public void setClosestNeighbours(List<Neighbour> closestNeighbours) {
        this.closestNeighbours = closestNeighbours;
    }

    public Integer getNumberOfNeighbours() {
        return numberOfNeighbours;
    }

    public void setNumberOfNeighbours(Integer numberOfNeighbours) {
        this.numberOfNeighbours = numberOfNeighbours;
    }

    public synchronized void add(Point point){

        List<Double> v1 = point.getVector();
        List<Double> v2 = reference.getVector();

        Double score = JensenShannon.similarity(v1,v2);

        if (closestNeighbours.size() < numberOfNeighbours){
            this.closestNeighbours.add(new Neighbour(point,score));
        }else if (score > minScore){
            this.closestNeighbours.add(new Neighbour(point,score));
            List<Neighbour> sorted = this.closestNeighbours.stream().sorted((a, b) -> a.getScore().compareTo(b.getScore())).collect(Collectors.toList());

            this.closestNeighbours.remove(sorted.get(0));
            this.minScore = sorted.get(1).score;

        }

    }

    @Override
    public String toString() {
        return "Neighbourhood{" +
                "reference=" + reference +
                ", closestNeighbours=" + closestNeighbours +
                ", numberOfNeighbours=" + numberOfNeighbours +
                '}';
    }
}
