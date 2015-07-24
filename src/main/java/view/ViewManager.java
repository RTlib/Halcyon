package view;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import model.node.HalcyonNode;
import model.node.HalcyonNodeInterface;
import model.list.HalcyonNodeRepository;
import model.list.HalcyonNodeRepositoryListener;
import model.list.ObservableCollection;
import model.list.ObservableCollectionListener;
import window.control.ConfigWindow;
import window.console.ConsoleInterface;
import window.FxConfigWindow;
import window.toolbar.ToolbarInterface;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.gui.dock.common.event.CDockableAdapter;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.util.xml.XElement;

/**
 * ViewManager class for managing Windows
 */
public class ViewManager
{
	/** the controller of the whole framework */
	private final CControl control;

	/** the {@link bibliothek.gui.dock.common.intern.CDockable}s showing some {@link HalcyonNode}s */
	private final List<HalcyonNodeDockable> pages = new LinkedList<>();

	/** the factory which creates new {@link HalcyonNodeFactory}s */
	private final HalcyonNodeFactory pageFactory;

	/** a set of {@link HalcyonNode}s */
	private final HalcyonNodeRepository nodes;

	/** the area on which the {@link HalcyonNode}s are shown */
	private final CWorkingArea workingArea;

	public ViewManager( CControl control, HalcyonNodeRepository nodes, HalcyonFrame.GUIBackend backend,
											ObservableCollection<ConsoleInterface> consoles,
											ObservableCollection<ToolbarInterface> toolbars)
	{
		this.control = control;
		this.nodes = nodes;

		pageFactory = new HalcyonNodeFactory();
		control.addMultipleDockableFactory( "page", pageFactory );

		workingArea = control.createWorkingArea( "halcyon node area" );
		workingArea.setLocation( CLocation.base().normalRectangle( 0, 0, 1, 1 ) );
		workingArea.setVisible( true );

		if(backend == HalcyonFrame.GUIBackend.JavaFX)
		{
			final FxConfigWindow configWindow = new FxConfigWindow( this );
			control.addDockable( configWindow );
			configWindow.setLocation( CLocation.base().normalWest( 0.3 ).south( 0.5 ) );
			configWindow.setVisible( true );
		}
		else if(backend == HalcyonFrame.GUIBackend.Swing)
		{
			final ConfigWindow configWindow = new ConfigWindow( this );
			control.addDockable( configWindow );
			configWindow.setLocation( CLocation.base().normalWest( 0.3 ).south( 0.5 ) );
			configWindow.setVisible( true );
		}

		toolbars.addListener(new ObservableCollectionListener<ToolbarInterface>()
		{
			@Override
			public void itemAdded( ToolbarInterface item)
			{
				control.addDockable( (DefaultSingleCDockable) item );
				((DefaultSingleCDockable)item).setLocation( CLocation.base().normalWest( 0.3 ).north( 0.5 ) );
				((DefaultSingleCDockable)item).setVisible( true );
			}

			@Override
			public void itemRemoved( ToolbarInterface item)
			{

			}
		} );

		consoles.addListener( new ObservableCollectionListener<ConsoleInterface>()
		{
			@Override public void itemAdded( ConsoleInterface item )
			{
				control.addDockable( (DefaultSingleCDockable) item );
				((DefaultSingleCDockable)item).setLocation( CLocation.base().normalEast( 0.7 ).south( 0.3 ) );
				((DefaultSingleCDockable)item).setVisible( true );
			}

			@Override public void itemRemoved( ConsoleInterface item )
			{

			}
		} );


		nodes.addListener( new HalcyonNodeRepositoryListener(){
			@Override
			public void nodeAdded( HalcyonNodeInterface node ){
				open( node );
			}
			@Override
			public void nodeRemoved( HalcyonNodeInterface node ){
				closeAll( node );
			}
		});
	}

	public HalcyonNodeRepository getNodes()
	{
		return nodes;
	}

	public CControl getControl() {
		return control;
	}

	public CWorkingArea getWorkingArea() {
		return workingArea;
	}

	public void open( HalcyonNodeInterface node ){

		for(final HalcyonNodeDockable n: pages)
		{
			if(n.getNode() == node) return;
		}

		final HalcyonNodeDockable page = new HalcyonNodeDockable( pageFactory );
		page.addCDockableStateListener( new CDockableAdapter(){
			@Override
			public void visibilityChanged( CDockable dockable ) {
				if( dockable.isVisible() ){
					pages.add( page );
				}
				else{
					pages.remove( page );
				}
			}
		});

		page.setNode( node );

		page.setLocation( CLocation.working( workingArea ).rectangle( 0, 0, 1, 1 ) );
		workingArea.add( page );
		page.setVisible( true );
	}

	public void closeAll( HalcyonNodeInterface node ){
		for( final HalcyonNodeDockable page : pages.toArray( new HalcyonNodeDockable[ pages.size() ] )){
			if( page.getNode()  == node ){
				page.setVisible( false );
				control.removeDockable( page );
			}
		}
	}


	/**
	 * A factory which creates {@link view.HalcyonNodeDockable}s.
	 */
	private class HalcyonNodeFactory implements MultipleCDockableFactory<HalcyonNodeDockable, HalcyonNodeLayout>
	{
		@Override
		public HalcyonNodeLayout create() {
			return new HalcyonNodeLayout();
		}

		@Override
		public HalcyonNodeDockable read( HalcyonNodeLayout layout ) {
			final String name = layout.getName();
			final HalcyonNodeInterface node = nodes.getNode( name );
			if( node == null )
				return null;
			final HalcyonNodeDockable page = new HalcyonNodeDockable( this );
			page.addCDockableStateListener( new CDockableAdapter(){
				@Override
				public void visibilityChanged( CDockable dockable ) {
					if( dockable.isVisible() ){
						pages.add( page );
					}
					else{
						pages.remove( page );
					}
				}
			});
			page.setNode( node );
			return page;
		}

		@Override
		public HalcyonNodeLayout write( HalcyonNodeDockable dockable ) {
			final HalcyonNodeLayout layout = new HalcyonNodeLayout();
			layout.setName( dockable.getNode().getName() );
			return layout;
		}

		@Override
		public boolean match( HalcyonNodeDockable dockable, HalcyonNodeLayout layout ){
			final String name = dockable.getNode().getName();
			return name.equals( layout.getName() );
		}
	}

	/**
	 * Describes the layout of one {@link HalcyonNodeDockable}
	 */
	private static class HalcyonNodeLayout implements MultipleCDockableLayout
	{
		/** the name of the picture */
		private String name;

		/**
		 * Sets the name of the picture that is shown.
		 * @param name the name of the picture
		 */
		public void setName( String name ) {
			this.name = name;
		}

		/**
		 * Gets the name of the picture that is shown.
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		@Override
		public void readStream( DataInputStream in ) throws IOException
		{
			name = in.readUTF();
		}

		@Override
		public void readXML( XElement element ) {
			name = element.getString();
		}

		@Override
		public void writeStream( DataOutputStream out ) throws IOException {
			out.writeUTF( name );
		}

		@Override
		public void writeXML( XElement element ) {
			element.setString( name );
		}
	}
}