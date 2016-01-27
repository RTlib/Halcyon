package utils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.SceneBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.WindowEvent;
import model.component.FxRunnable;

/**
 * RunFx is an utility class to start any inherited class from FxControl
 */
public class RunFx extends Application
{
	private FxRunnable ctrl;
	public RunFx( FxRunnable ctrl )
	{
		this.ctrl = ctrl;
	}

	@Override public void init()
	{
		ctrl.init();
	}

	@Override public void start( Stage primaryStage ) throws Exception
	{
		ctrl.start( primaryStage );
	}

	public static void start( FxRunnable ctrl )
	{
		new JFXPanel();
		Platform.runLater(new Runnable() {
			public void run() {
				RunFx fx = new RunFx( ctrl );

				Stage stage = StageBuilder.create()
						.scene( SceneBuilder.create()
								.width(320)
								.height(240)
								.root( LabelBuilder.create()
										.font( Font.font( "Arial", 54 ))
										.text("JavaFX")
										.build())
								.build())
						.onCloseRequest(new EventHandler<WindowEvent >() {
							@Override
							public void handle(WindowEvent windowEvent) {
								System.exit(0);
							}
						})
						.build();

				try
				{
					fx.init();
					fx.start( stage );
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
		});
	}
}