package it.polito.tdp.metroparis.model;

import java.util.List;

public class TestModel {

	public static void main(String[] args) {
		
		Model m = new Model();
		m.creaGrafo();
		
		//System.out.println(m.getGrafo());
		System.out.format("Creati %d vertici e %d archi\n", m.getGrafo().vertexSet().size(), m.getGrafo().edgeSet().size());
		
		Fermata f = m.getFermate().get(0);
		System.out.println("Parto da :"+f);
		
		List<Fermata> raggiungibili = m.fermateRaggiungibili(f);
		System.out.println("Fermate raggiunte :"+raggiungibili + "("+raggiungibili.size()+")");
		
		

		Fermata target = m.getFermate().get(150);
		System.out.println("Arrivo a :"+target);
		
		List<Fermata> percorso = m.percorsoFinoa(target);
		System.out.println(percorso);
		
		Fermata partenza = m.getFermate().get(2);
		Fermata arrivo = m.getFermate().get(57);
		List<Fermata> percorsominimo = m.trovaCamminoMinimo(partenza, arrivo);
		System.out.println("Il percorso minimo tra "+partenza +" e " +arrivo +" e': " +percorsominimo);
		
		
		
	}

}
