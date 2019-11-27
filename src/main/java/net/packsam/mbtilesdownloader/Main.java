package net.packsam.mbtilesdownloader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.packsam.mbtilesdownloader.config.PropertiesConfiguration;

/**
 * Main class.
 * 
 * @author osterrath
 *
 */
public class Main {

	/**
	 * Main method.
	 * 
	 * @param args
	 *            program arguments
	 * @return exit value
	 */
	public static void main(String[] args) {
		Options options = createCommandLineOptions();
		CommandLine cl;
		try {
			cl = parseCommandLineArgs(options, args);
		} catch (ParseException e1) {
			System.err.println(e1.getMessage());
			printUsage(options);
			return;
		}
		if (cl.hasOption("h")) {
			printUsage(options);
			return;
		}

		// read bounds
		String[] boundsS = cl.getOptionValues("b");
		double bounds[] = new double[boundsS.length];
		try {
			for (int i = 0; i < boundsS.length; i++) {
				bounds[i] = Double.parseDouble(boundsS[i]);
			}
		} catch (NumberFormatException e) {
			System.err.println("Invalid bounds!");
			printUsage(options);
			System.exit(1);
			return;
		}

		// read zoom levels
		String[] zoomsS = cl.getOptionValues("z");
		int zooms[] = new int[zoomsS.length];
		try {
			for (int i = 0; i < zoomsS.length; i++) {
				zooms[i] = Integer.parseInt(zoomsS[i], 10);
			}
		} catch (NumberFormatException e) {
			System.err.println("Invalid zoom levels!");
			printUsage(options);
			System.exit(1);
			return;
		}

		// read target MBtiles file
		File targetFile = new File(cl.getOptionValue("f"));

		// read options file
		File optionsFile = null;
		if (cl.hasOption("o")) {
			optionsFile = new File(cl.getOptionValue("o"));
			if (!optionsFile.exists() || !optionsFile.isFile() || !optionsFile.canRead()) {
				System.err.println("Invalid options file!");
				printUsage(options);
				System.exit(1);
				return;
			}
		}

		// create tiles downloader
		MBTilesDownloader mbTilesDownloader;
		try {
			if (optionsFile != null) {
				mbTilesDownloader = new MBTilesDownloader(bounds[0], bounds[1], bounds[2], bounds[3], zooms[0], zooms[1], targetFile, new PropertiesConfiguration(optionsFile));
			} else {
				mbTilesDownloader = new MBTilesDownloader(bounds[0], bounds[1], bounds[2], bounds[3], zooms[0], zooms[1], targetFile);
			}
		} catch (IOException e) {
			System.err.println("Could not create MBTiles file!");
			System.exit(2);
			return;
		}

		// start downloader
		mbTilesDownloader.download(Main::printProgress);
	}

	/**
	 * Prints the application usage.
	 * 
	 * @param options
	 *            command line options
	 */
	private static void printUsage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( //
				"java " + Main.class.getName(), //
				"Creates or updates an MBTiles file.", //
				options, //
				"Hint: You can get the bounding box from http://boundingbox.klokantech.com/", //
				true //
		);
	}

	/**
	 * Prints the current progress to the console.
	 * 
	 * @param progress
	 *            current progress
	 */
	private static Void printProgress(DownloadProgress progress) {
		System.out.println(progress.getFinishedTiles() + " / " + progress.getTotalTiles());
		return null;
	}

	/**
	 * Parses the command line arguments.
	 *
	 * @param options
	 *            command line options
	 * @param args
	 *            arguments
	 * @return parsed command line
	 * @throws ParseException
	 *             command line parameters could not be parsed
	 */
	private static CommandLine parseCommandLineArgs(Options options, String[] args) throws ParseException {
		DefaultParser parser = new DefaultParser();
		return parser.parse(options, args);
	}

	/**
	 * Creates the commend line options for the application.
	 * 
	 * @return command line options
	 */
	private static Options createCommandLineOptions() {
		Options options = new Options();
		options.addOption(Option.builder("h") //
				.longOpt("help") //
				.desc("prints this help") //
				.build() //
		);
		options.addOption(Option.builder("b") //
				.required() //
				.hasArgs() //
				.numberOfArgs(4) //
				.valueSeparator(',') //
				.type(Double.class).argName("boundaries") //
				.longOpt("bounds") //
				.desc("boundaries in the form '<western longitude>,<south latitude>,<eastern longitude>,<northern latitude>'") //
				.build() //
		);
		options.addOption(Option.builder("z") //
				.required() //
				.hasArgs() //
				.numberOfArgs(2) //
				.valueSeparator('-') //
				.type(Integer.class) //
				.argName("zoomlevels") //
				.longOpt("zoom") //
				.desc("zoom levels in the form '<min zoom>-<max zoom>'") //
				.build() //
		);
		options.addOption(Option.builder("f") //
				.required() //
				.hasArgs() //
				.numberOfArgs(1) //
				.argName("mbtilesfile") //
				.longOpt("file") //
				.desc("target MBTiles file to write") //
				.build() //
		);
		options.addOption(Option.builder("o") //
				.hasArgs() //
				.numberOfArgs(1) //
				.argName("optionsfile") //
				.longOpt("options") //
				.desc("options file") //
				.build() //
		);
		return options;
	}

}
