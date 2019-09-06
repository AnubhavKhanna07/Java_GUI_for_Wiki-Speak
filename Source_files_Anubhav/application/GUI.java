package application;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Scanner;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application{

	private ProcessInformation _processInfo; 
	
	// The "_processInfo" is used throughout the program to temporarily store the standard output of the bash processes
	
	private TextField _txtInput;
	private Button _searchButton;
	int _noOfSentencesToInclude;
	int _totalSentences;
	Stage _mainWindow;
	Scene _creationScene1;
	String _nameOfCreation;
	double audioLength;
	double videoLength;
	String _wordToSearch;
	String _creationsDirectory = "./Creations";
	TableView<Creation> _creationTable;
	Tab _tab1, _tab2;
	ObservableList<Creation> _creationsList;
	BorderPane _borderPaneCreationStep1;
	boolean _wasOverwritten;
	public static String _jarDir;
	boolean _searchWordFound;
	String _creationToPlayString;
	Button _createButton;
	Button _playButton;

	@Override
	public void start(Stage mainWindow) {

		// The following block of code gets the path of the folder which contains the executable jar file and puts that in the "_jarDir" variable. 
		CodeSource codeSource = GUI.class.getProtectionDomain().getCodeSource();
		File jarFile = null;
		try {
			jarFile = new File(codeSource.getLocation().toURI().getPath());
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		_jarDir = jarFile.getParentFile().getPath();

		// Setting up the Create creations tab (Tab 1)
		TabPane tabPane = new TabPane();
		_borderPaneCreationStep1 = new BorderPane();
		_tab1 = new Tab("Create a creation");
		Label label = new Label("Enter the word you want to search for:");
		_txtInput = new TextField();
		VBox vbox = new VBox(5);
		_searchButton = new Button("Search");
		vbox.getChildren().addAll(label, _txtInput, _searchButton);
		_borderPaneCreationStep1.setCenter(vbox);
		_tab1.setContent(_borderPaneCreationStep1);

		// Setting up the View/Play/Delete tab (Tab 2)
		_tab2 = new Tab("View/Play/Delete Creations");
		setupTab2();
		tabPane.getTabs().addAll(_tab1, _tab2);

		// Setting up the Stage
		_mainWindow = mainWindow;
		_mainWindow.setTitle("VARpedia");
		_creationScene1 = new Scene(tabPane,700,700);
		_mainWindow.setScene(_creationScene1);
		_mainWindow.show();

		// The following block of code handles various events
		_searchButton.setOnAction(e -> {
			createCreations();
		});
		_mainWindow.setOnCloseRequest(e -> {
			e.consume();
			closeVARpedia();
		});

	}

	// The following method is used to give a prompt before the VARpedia window is closed
	private void closeVARpedia() {
		Boolean shouldClose = ConfirmationBox.confirmNow("Closing prompt", "Are you sure you want to close VARpedia ?");
		if (shouldClose == true) {
			_mainWindow.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void setupTab2() {
		// Setting up columns for the creations table
		TableColumn<Creation, String> nameColumn = new TableColumn<>("Creations:");
		TableColumn<Creation, Integer> numberColumn = new TableColumn<>("No.");
		nameColumn.setMinWidth(600);
		numberColumn.setMinWidth(30);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("_creationName"));
		numberColumn.setCellValueFactory(new PropertyValueFactory<>("_creationNumber"));

		// Creating the table
		_creationTable = new TableView<>();
		_creationTable.setPlaceholder(new Label("There are no existing creations to display."));
		_creationTable.setItems(getCreations());
		_creationTable.getColumns().addAll(numberColumn, nameColumn);

		// Creating the GUI button for tab 2
		BorderPane borderPane2 = new BorderPane();
		HBox hboxTab2 = new HBox(10);
		_playButton = new Button("Play");
		Button deleteButton = new Button("Delete");
		Label tab2Instructions1 = new Label("Select a creation from the table below to play/delete it.");
		Label tab2Instructions2 = new Label("After making a selection, click on the appropriate button at the bottom of the window.");

		// Handling the clicking of the play and delete buttons
		_playButton.setOnAction(e -> {
			Creation creationToPlay = _creationTable.getSelectionModel().getSelectedItem();
			if (creationToPlay != null) {
				_playButton.setDisable(true);
				_creationToPlayString = creationToPlay.get_creationName();
				Thread threadCreationPlayWorker = new Thread(new CreationPlayWorker());
				threadCreationPlayWorker.start();
			}
		});

		deleteButton.setOnAction(e -> {
			Creation creationToDelete = _creationTable.getSelectionModel().getSelectedItem();
			if (creationToDelete != null) {
				String creationToDeleteString = creationToDelete.get_creationName();
				boolean shouldCreationBeDeleted = ConfirmationBox.confirmNow("Deletion Prompt", "Are you sure you want to delete the creation \""+creationToDeleteString+"\" ?");
				if (shouldCreationBeDeleted == true) {
					ProcessCreator.executeProcess("rm "+_creationsDirectory+"/\""+creationToDeleteString+"\".mp4");
					CustomAlert.showAlert(AlertType.INFORMATION, "Creation deleted", "The creation \""+creationToDeleteString+"\" has successfully been deleted.", "Click the OK button.");
					setupTab2();
				}
				else {
					CustomAlert.showAlert(AlertType.INFORMATION, "Creation deleted", "The creation \""+creationToDeleteString+"\" was not deleted.", "Click the OK button.");
				}
			}
		});

		// Setting up the tab 2 GUI components
		VBox vBoxInstructions = new VBox();
		vBoxInstructions.getChildren().addAll(tab2Instructions1, tab2Instructions2);
		hboxTab2.getChildren().addAll(_playButton, deleteButton);
		hboxTab2.setAlignment(Pos.CENTER);
		borderPane2.setTop(vBoxInstructions);
		borderPane2.setCenter(_creationTable);
		_tab2.setContent(borderPane2);
		borderPane2.setBottom(hboxTab2);
	}

	// The following method reads the list of existing creations from a text file and sets up creation
	// objects. It then puts them in a creations list
	public ObservableList<Creation> getCreations() {
		int creationNumber = 1;
		_creationsList = FXCollections.observableArrayList();
		_processInfo = ProcessCreator.executeProcess("ls "+_creationsDirectory+" -1 | grep .mp4 | sed 's/.mp4//' | sort");
		String creationStringList = _processInfo.getStdout();
		Scanner scanner = new Scanner(creationStringList);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			_creationsList.add(new Creation(line, new Integer(creationNumber)));
			creationNumber++;
		}
		scanner.close();
		return _creationsList;
	}

	// The following method starts the creation making process by first making the necessary directories if they are not already there
	// It then offloads the task of searching the word to a thread.
	public void createCreations() {
		_searchWordFound = true;
		ProcessCreator.executeProcess("mkdir ./Wiki_tool_files");
		ProcessCreator.executeProcess("mkdir ./Creations");

		_wordToSearch = _txtInput.getText();
		Thread threadWikitWorker = new Thread(new WikitWorker1());
		threadWikitWorker.start();
	}

	public void creationStep2(String sentences) {
		// Creating the GUI components
		Label label1 = new Label("The sentences are as follows:");
		Label label2 = new Label("Enter the number of sentences you want in the creation below: (between 1 and "+_totalSentences+")");
		Label label3 = new Label("Enter a name for the creation below:");
		TextArea txtArea = new TextArea(sentences);
		txtArea.setEditable(false);
		txtArea.setWrapText(true);
		TextField txtField1 = new TextField();
		TextField txtField2 = new TextField();
		_createButton = new Button("Create!");
		Button returnToWordSearchButton = new Button("Return to word search");
		_wasOverwritten = false;

		// Handling the clicking of the "create" button
		_createButton.setOnAction(e -> {
			_nameOfCreation = txtField2.getText();
			boolean satisfied = true;

			// The following code checks if the valid number of sentences has been entered
			try {
				_noOfSentencesToInclude = Integer.parseInt(txtField1.getText());
				if (_noOfSentencesToInclude < 1 || _noOfSentencesToInclude > _totalSentences) {
					satisfied = false;
					CustomAlert.showAlert(AlertType.ERROR, "Try Again", "Invalid number of sentences entered", "Please enter a number between 1 and "+_totalSentences);
				}
			}
			catch (Exception exceptionCaught) {
				satisfied = false;
				CustomAlert.showAlert(AlertType.ERROR, "Try Again", "Invalid number of sentences entered", "Please enter a number between 1 and "+_totalSentences);
			}

			// The following code checks if the provided creation name is null or empty
			if (satisfied == true) {
				if (_nameOfCreation == null || _nameOfCreation == "" || _nameOfCreation.length() == 0 ) {
					satisfied = false;
					CustomAlert.showAlert(AlertType.ERROR, "Try Again", "Creation name cannot be empty", "Please enter a name for your creation");
				}
			}

			// The following code checks if any invalid characters are present in the creation name
			if (satisfied == true) {
				if (_nameOfCreation.matches(".*[$*|\\<>?/:\"\'`]+.*") || _nameOfCreation.contains("\\")) {
					satisfied = false;
					CustomAlert.showAlert(AlertType.ERROR, "Try Again", "Creation name has invalid characters", "Try again as the creation name cannot have the following characters:\n '$*|\\<>?`/:\"");
				}
			}

			// The following code checks if a creation which the provided name already exists
			boolean doesCreationAlreadyExist = false;
			if (satisfied == true) {
				for (Creation tempCreation: _creationsList) {
					if ((tempCreation.get_creationName()).equals(_nameOfCreation)) {
						doesCreationAlreadyExist = true;
						satisfied = false;
						break;
					}
				}
			}
			if (doesCreationAlreadyExist == true) {
				boolean shouldCreationBeOverwritten = ConfirmationBox.confirmNow("Overwriting Prompt", "A creation by this name already exists. Do you want to overwrite the creation \""+_nameOfCreation+"\" ?");
				if (shouldCreationBeOverwritten == true) {
					ProcessCreator.executeProcess("rm "+_creationsDirectory+"/\""+_nameOfCreation+"\".mp4");
					_wasOverwritten = true;
					satisfied = true;
				}
				else {
					CustomAlert.showAlert(AlertType.INFORMATION, "Creation Not Overwrittem", "The creation \""+_nameOfCreation+"\" was not overwritten.", "Click the OK button.");
				}
			}

			// Creating the creation if all requirements are satisfied
			if (satisfied == true) {
				_createButton.setDisable(true);
				Thread threadCreationWorker = new Thread(new CreationWorker());
				threadCreationWorker.start();
			}
		});

		// Handling the clicking of "return to word search" button
		returnToWordSearchButton.setOnAction(e -> {
			_tab1.setContent(_borderPaneCreationStep1);
		});

		// Setting up the visual content of Tab 1 for the 2nd step of creating a creation (which is to select the number of sentences and to enter a name)
		HBox hBoxCreationStep2Buttons = new HBox(10);
		hBoxCreationStep2Buttons.getChildren().addAll(_createButton, returnToWordSearchButton);
		hBoxCreationStep2Buttons.setAlignment(Pos.CENTER);
		VBox vBoxCreationStep2 = new VBox(10);
		vBoxCreationStep2.getChildren().addAll(label1, txtArea, label2, txtField1, label3, txtField2, hBoxCreationStep2Buttons);
		_tab1.setContent(vBoxCreationStep2);
	}

	// The call() method of the following class has the code to search a word on Wikipedia
	// If the word is valid, the done() method calls the creationStep2() to start the audio and video making process. 
	// If the word is invalid, the done() method shows an error prompt.
	private class WikitWorker1 extends Task<Void> {

		@Override
		protected Void call() throws Exception {

			_processInfo = ProcessCreator.executeProcess("wikit "+_wordToSearch+" | grep \""+_wordToSearch+" not found\" | wc -l");
			int wordNotFoundCounter = (int)(Double.parseDouble((_processInfo.getStdout())));
			if (wordNotFoundCounter == 1) {
				_searchWordFound = false;
			}

			if (_searchWordFound == true) {
				ProcessCreator.executeProcess("wikit "+_wordToSearch+" | sed 's/\\([.!?]\\) \\([[:upper:]]\\)/\\1\\n\\2/g' | sed 's/  //g' > ./Wiki_tool_files/Wiki_sentences.txt");
				_processInfo = ProcessCreator.executeProcess("cat -n ./Wiki_tool_files/Wiki_sentences.txt");
				ProcessInformation processInfo2 = ProcessCreator.executeProcess("cat ./Wiki_tool_files/Wiki_sentences.txt | wc -l");
				_totalSentences = (int)Double.parseDouble(processInfo2.getStdout());
			}
			return null;
		}

		@Override
		protected void done() {
			Platform.runLater(() -> {
				if (_searchWordFound == true) {
					creationStep2(_processInfo.getStdout());
				}
				else {
					CustomAlert.showAlert(AlertType.ERROR, "Try Again", "The word \""+_wordToSearch+"\" was not found.", "Please enter a valid word to search for.");
				}
			});
		}
	}

	// The call() method of the following class has the code to do the audio and video processing for the
	// creation. The done() method shows the appropriate information prompts.
	private class CreationWorker extends Task<Void> {

		@Override
		protected Void call() throws Exception {

			//Creating the audio file for the selected sentences
			ProcessCreator.executeProcess("sed -n 1,"+_noOfSentencesToInclude+"\\p ./Wiki_tool_files/Wiki_sentences.txt > ./Wiki_tool_files/Selected_sentences.txt");
			ProcessCreator.executeProcess("espeak -f ./Wiki_tool_files/Selected_sentences.txt -w ./Wiki_tool_files/Wiki_audio.wav");
			_processInfo = ProcessCreator.executeProcess("soxi -D ./Wiki_tool_files/Wiki_audio.wav");
			audioLength = Double.parseDouble(_processInfo.getStdout());
			_processInfo = ProcessCreator.executeProcess("echo \""+audioLength+"+1.5\" | bc");
			videoLength = Double.parseDouble(_processInfo.getStdout());
			ProcessCreator.executeProcess("ffmpeg -y -f lavfi -i color=c=blue:s=720x480:d="+videoLength+" -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=40: fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+_wordToSearch+"\" \\./Wiki_tool_files/Wiki_video.mp4");
			ProcessCreator.executeProcess("ffmpeg -i ./Wiki_tool_files/Wiki_video.mp4 -i ./Wiki_tool_files/Wiki_audio.wav -c:v copy -c:a aac -strict experimental ./Creations/\""+_nameOfCreation+"\".mp4");
			ProcessCreator.executeProcess("rm -r ./Wiki_tool_files/");
			return null;
		}

		@Override
		protected void done() {
			Platform.runLater(() -> {
				if (_wasOverwritten == true) {
					CustomAlert.showAlert(AlertType.INFORMATION, "Creation created", "The creation \""+_nameOfCreation+"\" has successfully been overwritten.", "Click the OK button.");
				}
				else {
					CustomAlert.showAlert(AlertType.INFORMATION, "Creation created", "The creation \""+_nameOfCreation+"\" has successfully been created.", "Click the OK button.");
				}
				_createButton.setDisable(false);
				setupTab2();
				_tab1.setContent(_borderPaneCreationStep1);
			});
		}

	}

	// The call() method of the following class has the code to play the creation.
	// The done() method enables the play button.
	private class CreationPlayWorker extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			ProcessCreator.executeProcess("ffplay -autoexit "+_creationsDirectory+"/\""+_creationToPlayString+"\".mp4");
			return null;
		}

		@Override
		protected void done() {
			Platform.runLater(() -> {
				_playButton.setDisable(false);
			});
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
