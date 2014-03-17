package main;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import com.google.common.collect.Lists;

public class DrmaaMain {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Expected two arguments but found: "
					+ args.length + " with values: " + Arrays.toString(args));
			System.err
					.println("Usage: qrsh -cwd java -jar drmaaTest.jar master numJobs");
		}
		int jobArg;
		try {
			jobArg = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err
					.println("Expected second argument to be a number, but was: "
							+ args[1]);
			return;
		}
		if (args[0].equalsIgnoreCase("master")) {
			master(jobArg);
		} else if (args[0].equalsIgnoreCase("worker")) {
			worker(jobArg);
		} else {
			System.err
					.println("Usage: qrsh -cwd java -jar drmaaTest.jar master numJobs");
		}

	}

	public static void master(int numJobs) {
		SessionFactory factory = SessionFactory.getFactory();
		Session session = factory.getSession();
		try {
			session.init("");
			JobTemplate jt = session.createJobTemplate();

			jt.setRemoteCommand("java");
			String home = java.lang.System.getProperty("user.dir");
			jt.setWorkingDirectory(home);
			List<String> jobIds = Lists.newArrayList();
			for (int i = 0; i < numJobs; i++) {
				jt.setErrorPath(":" + home + "/error" + i + ".txt");
				jt.setOutputPath(":" + home + "/out" + i + ".txt");
				jt.setArgs(Lists
						.newArrayList(
								"-Djava.library.path=/opt/uge816/lib/lx-amd64/:/opt/ibm/ILOG/CPLEX_Studio125/cplex/bin/x86-64_sles10_4.1/",
								"-jar", home + "/drmaaTest.jar", "worker",
								Integer.toString(i)));
				String id = session.runJob(jt);
				System.out.println("Your job has been submitted with id " + id);
			}

			session.deleteJobTemplate(jt);
			session.synchronize(jobIds, Session.TIMEOUT_WAIT_FOREVER, true);
			System.out.println("All jobs finished!");
			session.exit();
			List<String> answers = Lists.newArrayList();
			for (int i = 0; i < numJobs; i++) {
				String ans = "error";
				String jobName = "job" + i + ".txt";
				File file = new File(jobName);
				long msToWait = 2000;
				while (!file.exists()) {
					System.out.println("waiting for file system to show "
							+ jobName + ".  Sleeping " + msToWait + "ms");
					try {
						Thread.sleep(msToWait);
					} catch (InterruptedException e) {
					}
					msToWait = 2 * msToWait;
				}
				if (file.exists()) {
					try {
						BufferedReader reader = new BufferedReader(
								new FileReader(jobName));
						String firstLine = reader.readLine();
						if (firstLine != null) {
							ans = firstLine;
						}
						reader.close();
					} catch (IOException e) {
						ans = "I/O error";
					}
				}
				answers.add(ans);
			}
			System.out.println("Answers: " + answers);
		} catch (DrmaaException e) {
			System.out.println("Error: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static void worker(int jobId) {

		try {
			IloCplex cplex = new IloCplex();
			IloNumVar var = cplex.numVar(0, 2 * jobId + 1);
			cplex.addMaximize(var);
			cplex.solve();
			BufferedWriter writer = new BufferedWriter(new FileWriter("job"
					+ jobId + ".txt"));
			writer.write(Double.toString(cplex.getObjValue()));
			writer.close();
		} catch (IloException e) {
			System.err.println("Job " + jobId + " crashed!");
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.err.println("Job " + jobId + " crashed!");
			throw new RuntimeException(e);
		}

	}
}
