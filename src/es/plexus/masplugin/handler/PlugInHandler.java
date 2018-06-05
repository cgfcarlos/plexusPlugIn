package es.plexus.masplugin.handler;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import es.plexus.masplugin.models.Aviso;
import es.plexus.masplugin.models.Fichero;
import es.plexus.masplugin.models.Log;

public class PlugInHandler {

	private String rootPath = "C:\\Proyectos\\2018\\686-035-Abanca-AdaptacionPlataformaMultinegocio";

	public PlugInHandler() {
		super();
	}

	public String getRootPath() {
		return rootPath;
	}

	public List<Log> listarLogs(String app) {
		File directioLogs = new File(rootPath, "trabajo/logs/" + app);
		List<Log> logs = new ArrayList<Log>();
		if (directioLogs.exists()) {

			File[] fl = directioLogs.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			if (null != fl) {
				for (File f : fl) {
					Log l = new Log();
					l.setNombre(f.getName());
					l.setFecha(new Date(f.lastModified()));
					logs.add(l);
				}
			}

		}
		return logs;
	}

	public List<Fichero> listarFicheros(String app, String uid) {
		File logs = new File(rootPath, "trabajo/logs/" + "ANPM" + "/" + uid);
		List<Fichero> ficheros = new ArrayList<Fichero>();
		if (logs.exists()) {
			Log theLog = new Log();
			theLog.setFicherosModificados(new ArrayList<>());
			theLog.setFicherosNuevos(new ArrayList<>());
			walk(logs, new File(logs, "ANPM"), theLog);
			for (Fichero fichero : theLog.getFicherosModificados()) {
				ficheros.add(fichero);
			}
			for (Fichero fichero : theLog.getFicherosNuevos()) {
				ficheros.add(fichero);
			}
		}
		return ficheros;
	}

	private void walk(File path, File root, Log log) {
		File[] list = root.listFiles();

		if (list != null) {
			for (File f : list) {
				if (f.isDirectory()) {
					walk(path, f, log);
				} else {
					String name = f.getAbsolutePath().substring(path.getAbsolutePath().length() + 1);
					Fichero fichero = new Fichero();
					fichero.setPath(name.replace(File.separatorChar, '-'));
					fichero.setNombre(f.getName());
					if (f.getName().endsWith(".original")) {
						fichero.setNombre(fichero.getNombre().substring(0, fichero.getNombre().length() - 9));
						fichero.setPath(fichero.getPath().substring(0, fichero.getPath().length() - 9));
						log.getFicherosModificados().add(fichero);
					} else if (f.getName().endsWith(".new")) {
					} else {
						// log.getFicherosNuevos().add(fichero);
					}
				}
			}
		}
	}

	public List<Aviso> listarAvisos(String app, Integer skip, Integer limit, String tipo, Boolean soloPendientes,
			Boolean sinResolver, Integer page) {
		try {
			if (null == page) {
				page = 0;
			}
			if (null == limit) {
				limit = 20;
			}
			if (null == skip) {
				skip = 0;
			}
			List<Aviso> apps = new ArrayList<>();
			File f = new File(rootPath, "trabajo/excels/salida/" + app + "/" + app + ".xlsx");
			FileInputStream fin = new FileInputStream(f);
			Workbook book = WorkbookFactory.create(fin);
			fin.close();
			Sheet sheetAt = book.getSheetAt(1);
			int desde = 1 + skip + (limit * page);

			for (int i = desde; i <= sheetAt.getLastRowNum(); i++) {
				Row row = sheetAt.getRow(i);
				String accion = row.getCell(4).getStringCellValue();
				String rowTipo = row.getCell(1).getStringCellValue();
				if (tipo != null && !tipo.equals(rowTipo)) {
					continue;
				}
				if (Boolean.TRUE.equals(soloPendientes) && !"".equals(accion)) {
					continue;
				}
				if (Boolean.TRUE.equals(sinResolver) && ("SI".equals(accion) || "NO".equals(accion))) {
					continue;
				}
				Aviso aviso = new Aviso();
				aviso.setAccion(accion);
				aviso.setCategoria(rowTipo);
				aviso.setCodigo(row.getCell(3).getStringCellValue());
				aviso.setSentencia(row.getCell(5).getStringCellValue());
				aviso.setClase(row.getCell(6).getStringCellValue());
				aviso.setLinea((int) row.getCell(7).getNumericCellValue());
				aviso.setColumna((int) row.getCell(8).getNumericCellValue());
				// aviso.setId(String.valueOf(row.getRowNum()));
				aviso.setId(row.getCell(9).getStringCellValue());
				apps.add(aviso);
				// if (apps.size() == limit) {
				// break;
				// }
			}
			return apps;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void modificarAviso(String app, String id, Aviso aviso) {
		try {
			File f = new File(rootPath, "trabajo/excels/salida/" + app + "/" + app + ".xlsx");
			FileInputStream fin = new FileInputStream(f);

			Workbook book = WorkbookFactory.create(fin);
			fin.close();
			Sheet sheetAt = book.getSheetAt(1);
			Row row = sheetAt.getRow(Integer.parseInt(id));
			row.getCell(4).setCellValue(aviso.getAccion());

			FileOutputStream fout = new FileOutputStream(f);
			book.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
