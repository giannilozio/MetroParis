package it.polito.tdp.metroparis.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	private class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultWeightedEdge>{

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {		
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
			Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());		// ESTREMI DELL ARCO
			Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());

			/*
			 * SE IL grafo e' orientato ( come in qst caso ) source == parent, target == child 
			 * altrimenti potrebbe essere anche il contrario
			 */
			if( !backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex)) {//  SE CHILD SCONOSCIUTO E PARENT CONOSCIUTO
				backVisit.put(targetVertex, sourceVertex);								 	
			}else if (!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex))
				backVisit.put(sourceVertex, targetVertex);
		
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {	
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
		}
		
	}
	
	private Graph<Fermata, DefaultWeightedEdge> grafo; // GRAFO PESATO ( NON PIU' DEFAULTEDGE )
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	private Map<Fermata,Fermata> backVisit;  // mi salvo gli archi che ho visitato qui' dentro
	
	
	
	public void creaGrafo() {
		//CREO IL GRAFO
		grafo= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class); // GRAFO PESATO
		
		
		MetroDAO dao = new MetroDAO();
		fermate=dao.getAllFermate(); 			//IL DAO MI DA TUTTE LE FERMATE
		
		fermateIdMap = new HashMap <>();
		// METTO TUTTE LE FERMATE NELL IDMAP
		for(Fermata f : fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		//AGGIUNGO I VERTICI
		Graphs.addAllVertices(grafo, fermate); //LE AGGIUNGO
		
		
		//AGGIUNGO GLI ARCHI (opzione 2) piu' veloce
		
		for(Fermata partenza : grafo.vertexSet()) {
		List<Fermata> arrivi = dao.stazioneArrivo(partenza, fermateIdMap);
		
		for(Fermata arrivo: arrivi) {  			// PER QUELLA PARTENZA SEGNO TUTTI GLI ARCHI DI ARRIVO
			grafo.addEdge(partenza, arrivo);
		}
	}
		//AGGIUNGO  I PESI AGLI ARCHI 
		List<ConnessioneVelocita> archipesati = dao.getConnessioniVelocita();
		for(ConnessioneVelocita cp : archipesati) {
			Fermata partenza = fermateIdMap.get(cp.getStazP());
			Fermata arrivo = fermateIdMap.get(cp.getStazA());
			double distanza = LatLngTool.distance(partenza.getCoords(), arrivo.getCoords(),LengthUnit.KILOMETER);
			double peso = distanza / cp.getVelocita() * 3600; // TEMPO IN SEC
			
			grafo.setEdgeWeight(partenza, arrivo, peso);
			//Graphs.addEdgeWithVertices(grafo, partenza, arrivo,peso);
		}
}

	public List<Fermata> fermateRaggiungibili(Fermata source){
		//CREO UN ITERATORE E LO ASSOCIO A QUESTO GRAFO, INIZIALIZZA A UN PUNTO DI PARTENZA(RANDOM SE NON SPECIFICO)
		
		List<Fermata> result = new ArrayList<Fermata>();
		backVisit = new HashMap<>();
		
		GraphIterator<Fermata, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, source);
		//GraphIterator<Fermata,DefaultWeightedEdge> it = new DepthFirstIterator<>(grafo,source);		// VISITA IN PROFONDITA'
		
		it.addTraversalListener(new Model.EdgeTraversedGraphListener()); // aggiungo il listener
		
		backVisit.put(source,null);
		
		
		while(it.hasNext()) {			// SE CI SONO VERTICI SUCESSIVI
			result.add(it.next());		// AGGIUNGE UN ELEMENTO E AVANZA ALL'ELEMENTO SUCCESSIVO
		}
		
	//	System.out.println(backVisit);
		
		return result;
		
	}
	
	public List<Fermata> percorsoFinoa (Fermata target){
		if(!backVisit.containsKey(target)) {
			// il target nn e' raggiungibile dalla source
			return null;
		}
		
		List<Fermata> percorso = new LinkedList<>();
		
		Fermata f = target;

		while( f!=null) {
		percorso.add(0,f);
		f= backVisit.get(f);
		}
		return percorso;
	}


	public Graph<Fermata, DefaultWeightedEdge> getGrafo() {
		return grafo;
	}



	public List<Fermata> getFermate() {
		return fermate;
	}

	public List<Fermata> trovaCamminoMinimo(Fermata partenza, Fermata arrivo){
		DijkstraShortestPath<Fermata,DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(grafo);
		GraphPath<Fermata,DefaultWeightedEdge> path = dijkstra.getPath(partenza,arrivo);
		return path.getVertexList(); 		
	}

	

}
