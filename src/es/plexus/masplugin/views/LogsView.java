package es.plexus.masplugin.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;

import es.plexus.masplugin.models.Aviso;
import es.plexus.masplugin.models.Fichero;
import es.plexus.masplugin.models.Log;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.jface.action.*;
//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class LogsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "es.plexus.masplugin.views.LogsView";

	@Inject
	IWorkbench workbench;

	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private String rootPath = "C:\\Proyectos\\2018\\686-035-Abanca-AdaptacionPlataformaMultinegocio";
	private String eclipsePath = "C:\\Program Files\\eclipse\\eclipse.exe";

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			if (!obj.toString().contains(".java")) {
				return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			} else {
				return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		List<Log> logs = listarLogs("ANPM");
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(logs);
		viewer.setLabelProvider(new ViewLabelProvider());

		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(viewer.getControl(), "es.plexus.mas-plug-in.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				LogsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				List<Log> logs = listarLogs("ANPM");
				viewer.setInput(logs);
			}
		};
		action1.setText("Volver al listado");
		action1.setToolTipText("Volver al listado de logs");
		action1.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		action2 = new Action() {
			public void run() {
				List<Aviso> avisos = listarAvisos("ANPM", null, null, null, null, null, null);
				viewer.setInput(avisos);
			}
		};
		action2.setText("Leer Excel");
		action2.setToolTipText("Listar excel");
		action2.setImageDescriptor(workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				if (!obj.toString().contains(".java") && !obj.toString().contains(":")) {
					List<Fichero> ficheros = listarFicheros("ANPM", obj.toString());
					viewer.setInput(ficheros);
				} else if (!obj.toString().contains(".java") && obj.toString().contains(":")) {
					String path = obj.toString().split(":")[0];
					String linea = obj.toString().split(":")[1];
					File fPath = new File(rootPath, "trabajo/codigo/" + "ANPM" + "/src/main/java/"
							+ path.replace('.', '/') + ".java");

					ProcessBuilder pb = new ProcessBuilder(eclipsePath, fPath.getAbsolutePath() + ":" + linea);
					try {
						pb.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					File fPath = new File(rootPath, "trabajo/codigo/" + obj.toString().replace('-', '/'));
					ProcessBuilder pb = new ProcessBuilder(eclipsePath, fPath.getAbsolutePath());
					try {
						pb.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	// private void showMessage(String message) {
	// MessageDialog.openInformation(viewer.getControl().getShell(), "Logs View",
	// message);
	// }

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private List<Log> listarLogs(String app) {
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

	private List<Fichero> listarFicheros(String app, String uid) {
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
						log.getFicherosNuevos().add(fichero);
					}
				}
			}
		}
	}

	private List<Aviso> listarAvisos(String app, Integer skip, Integer limit, String tipo, Boolean soloPendientes,
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
//				if (apps.size() == limit) {
//					break;
//				}
			}
			return apps;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
