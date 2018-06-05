package es.plexus.masplugin.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.*;

import es.plexus.masplugin.handler.PlugInHandler;
import es.plexus.masplugin.models.Aviso;
import es.plexus.masplugin.models.Fichero;
import es.plexus.masplugin.models.Log;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.*;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import java.io.File;
import java.io.IOException;
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
	private String eclipsePath = "C:\\Program Files\\eclipse\\eclipse.exe";
	private Composite container;
	private PlugInHandler handler = new PlugInHandler();

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
		container = parent;
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		List<Log> logs = handler.listarLogs("ANPM");
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
				TableColumn[] columns = viewer.getTable().getColumns();
				for (TableColumn tc : columns) {
					tc.dispose();
				}
				viewer.getTable().setHeaderVisible(false);
				viewer.getTable().setLinesVisible(false);
				List<Log> logs = handler.listarLogs("ANPM");
				viewer.setContentProvider(ArrayContentProvider.getInstance());
				viewer.setInput(logs);
				viewer.setLabelProvider(new ViewLabelProvider());
			}
		};
		action1.setText("Volver al listado");
		action1.setToolTipText("Volver al listado de logs");
		action1.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		action2 = new Action() {
			public void run() {
				List<Aviso> avisos = handler.listarAvisos("ANPM", null, null, null, null, null, null);
				viewer.setInput(avisos);
				createViewer(container);
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
					List<Fichero> ficheros = handler.listarFicheros("ANPM", obj.toString());
					viewer.setInput(ficheros);
				} else if (!obj.toString().contains(".java") && obj.toString().contains(":")) {
					Aviso aviso = (Aviso) obj;
					String path = aviso.getId().split(":")[0];
					String linea = aviso.getId().split(":")[1];
					File fPath = new File(handler.getRootPath(),
							"trabajo/codigo/" + "ANPM" + "/src/main/java/" + path.replace('.', '/') + ".java");

					ProcessBuilder pb = new ProcessBuilder(eclipsePath, fPath.getAbsolutePath() + ":" + linea);
					try {
						pb.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Fichero fichero = (Fichero) obj;
					File fPath = new File(handler.getRootPath(),
							"trabajo/codigo/" + fichero.getPath().replace('-', '/'));
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

	private void createViewer(Composite parent) {
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(handler.listarAvisos("ANPM", null, null, null, null, null, null));
		getSite().setSelectionProvider(viewer);
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {

		String[] titles = { "Categoria", "Clase", "Literal", "Accion", "Codigo", "Linea", "Columna", "ExcelId" };
		int[] bounds = { 100, 100, 100, 100, 100, 100, 100, 100 };

		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getCategoria();
			}
		});

		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getClase();
			}
		});

		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getCodigo();
			}
		});

		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getAccion();
			}
		});

		col.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object arg0, Object arg1) {
				arg0 = arg1;
			}

			@Override
			protected Object getValue(Object arg0) {
				return arg0.toString();
			}

			@Override
			protected CellEditor getCellEditor(Object arg0) {
				final ComboBoxCellEditor editor = new ComboBoxCellEditor(parent, getPossibleFilterValues(),
						SWT.READ_ONLY);
				((CCombo) editor.getControl()).addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent arg0) {
						IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
						String filterValue = sel.getFirstElement().toString();
						// Modificar excel desde aqui?

					}
				});
				return editor;
			}

			@Override
			protected boolean canEdit(Object arg0) {
				return true;
			}
		});

		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getSentencia();
			}
		});

		col = createTableViewerColumn(titles[5], bounds[5], 5);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getLinea().toString();
			}
		});

		col = createTableViewerColumn(titles[6], bounds[6], 6);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getColumna().toString();
			}
		});

		col = createTableViewerColumn(titles[7], bounds[7], 7);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Aviso aviso = (Aviso) element;
				return aviso.getId();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);

		if (colNumber == 3) {

		}
		return viewerColumn;
	}

	private String[] getPossibleFilterValues() {
		return new String[] { "SI", "NO", "DUDA" };
	}
}
