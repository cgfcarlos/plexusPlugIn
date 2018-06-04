package es.plexus.masplugin.models;

import java.util.Date;
import java.util.List;

public class Log {
	private String nombre;
	private Date fecha;
	private List<Fichero> ficherosModificados;
	private List<Fichero> ficherosNuevos;

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Fichero> getFicherosModificados() {
		return ficherosModificados;
	}

	public void setFicherosModificados(List<Fichero> ficherosModificados) {
		this.ficherosModificados = ficherosModificados;
	}

	public List<Fichero> getFicherosNuevos() {
		return ficherosNuevos;
	}

	public void setFicherosNuevos(List<Fichero> ficherosNuevos) {
		this.ficherosNuevos = ficherosNuevos;
	}
	
	@Override
	public String toString() {
		return this.getNombre();
	}
}
