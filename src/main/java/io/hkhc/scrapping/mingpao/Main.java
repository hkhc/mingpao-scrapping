package io.hkhc.scrapping.mingpao;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.hkhc.utils.FileUtils;

public class Main {

	private static class Args {

		@Parameter(
				names = "-date",
				required = true,
				order = 1,
				description = "Date of paper to be extracted (yyyy-mm-dd)")
		public String selectedDate;

		@Parameter(
				names = "-dir",
				required = true,
				order = 2,
				description = "Directory for output")
		public String baseDirectory;

		@Parameter(
				names = "-username",
				required = true,
				order = 3,
				description = "Username to login")
		public String username;

		@Parameter(
				names = "-password",
				required = true,
				order = 4,
				description = "Password to login")
		public String password;

		@Parameter(
				names = "-start",
				required = false,
				order = 5,
				description = "Starting page to be extracted")
		public String startPage;

		@Parameter(names = "-help", help = true, hidden=true)
		public boolean help;

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		System.setProperty("file.encoding", "utf-8");
		System.setProperty("user.language", "zh");

		Args arg = new Args();

		JCommander commander = JCommander.newBuilder()
				.addObject(arg)
				.build();

		try {
			commander.parse(args);
		}
		catch (ParameterException e) {
			System.out.println(e.getMessage());
			arg.help=true;
		}

		if (arg.help) {
			commander.usage();
			return;
		}

		System.out.println("baseDirectory " + arg.baseDirectory);
		System.out.println("date " + arg.selectedDate);
		FileUtils.ensureDirectory(arg.baseDirectory);

		Scrapper scrapper = new Scrapper()
				.username(arg.username)
				.password(arg.password)
				.selectedDate(arg.selectedDate)
				.outputDirectory(arg.baseDirectory);

		if (arg.startPage!=null) scrapper.startPage(arg.startPage);

		scrapper.scrap();


	}

}
