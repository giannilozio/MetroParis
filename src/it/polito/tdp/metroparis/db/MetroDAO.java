package it.polito.tdp.metroparis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metroparis.model.ConnessioneVelocita;
import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Linea;

public class MetroDAO {

	public List<Fermata> getAllFermate() {

		final String sql = "SELECT id_fermata, nome, coordx, coordy FROM fermata ORDER BY nome ASC";
		List<Fermata> fermate = new ArrayList<Fermata>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"), new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy")));
				fermate.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return fermate;
	}

	public List<Linea> getAllLinee() {
		final String sql = "SELECT id_linea, nome, velocita, intervallo FROM linea ORDER BY nome ASC";

		List<Linea> linee = new ArrayList<Linea>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Linea f = new Linea(rs.getInt("id_linea"), rs.getString("nome"), rs.getDouble("velocita"),
						rs.getDouble("intervallo"));
				linee.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return linee;
	}

	
		public boolean esisteConnessione(Fermata partenza, Fermata arrivo) {
			String sql = "SELECT COUNT(*) AS cnt FROM connessione WHERE id_stazP=? AND id_stazA=?";
			
			Connection conn = DBConnect.getConnection();
			PreparedStatement st;
			try {
			st = conn.prepareStatement(sql);
			st.setInt(1, partenza.getIdFermata());
			st.setInt(2,arrivo.getIdFermata());
			
			ResultSet rs = st.executeQuery();
			rs.next(); // MI POSIZIONO SULLA PRIMA RIGA
			
			int numero = rs.getInt("cnt"); // >0 SE C'E CONNESSIONE
			conn.close();
			if(numero>0)
				return true;
			else
				return false;
			}catch(SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		public List<Fermata> stazioneArrivo(Fermata partenza, Map<Integer, Fermata> fermateIdMap) {
			
			String sql= "SELECT id_stazA FROM connessione WHERE id_stazP=?"; // PER OGNI STAZIONEP MI DA TUTTE QUELLE DI ARRIVO CORRISPONDENTI
			
			Connection conn = DBConnect.getConnection();
			try {
				PreparedStatement st = conn.prepareStatement(sql);
				st.setInt(1,partenza.getIdFermata());
				ResultSet rs = st.executeQuery();
				
				List<Fermata> result = new ArrayList<>();
				while(rs.next()) {
					//result.add(new Fermata(rs.getInt("id_stazA"),null,null));
					result.add(fermateIdMap.get(rs.getInt("id_stazA"))); // INVECE DI CREARNE UNA NUOVA PRENDO DALLA MAP QUEI DATI
					
				}
				conn.close();
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			
			
		}
		
		public List<ConnessioneVelocita> getConnessioniVelocita(){
			String sql = "SELECT connessione.id_stazP, connessione.id_stazA, MAX(linea.velocita) AS velocita FROM connessione, linea WHERE connessione.id_linea=linea.id_linea "
					+"GROUP BY connessione.id_stazP, connessione.id_stazA" ;
			
			try {
				Connection conn = DBConnect.getConnection();
				PreparedStatement st;
				st = conn.prepareStatement(sql);
				ResultSet rs = st.executeQuery();
			
			List<ConnessioneVelocita> result = new ArrayList<>();
			while(rs.next())  {// MI POSIZIONO SULLA PRIMA RIGA
			
				ConnessioneVelocita item = new ConnessioneVelocita(rs.getInt("id_stazP"),rs.getInt("id_stazA"),rs.getDouble("velocita"));
						result.add(item);
			
				
		}conn.close();
		return result;
		}catch(SQLException e) {
			
		}
		
		return null;	
		}
	
		

}
