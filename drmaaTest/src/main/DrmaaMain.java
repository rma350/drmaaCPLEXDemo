package main;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import com.google.common.collect.Lists;

public class DrmaaMain {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Expected two arguments but found: " + args.length
          + " with values: " + Arrays.toString(args));
      System.err.println("Usage: java -jar drmaaTest.jar master numJobs");
    }
    int jobArg;
    try {
      jobArg = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      System.err.println("Expected second argument to be a number, but was: "
          + args[1]);
      return;
    }
    if (args[0].equalsIgnoreCase("master")) {
      master(jobArg);
    } else if (args[0].equalsIgnoreCase("worker")) {
      worker(jobArg);
    } else {
      System.err.println("Usage: java -jar drmaaTest.jar master numJobs");
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
      System.out.println("home: " + home);
      System.out.println("init working directory: " + jt.getWorkingDirectory());
      jt.setWorkingDirectory(home);
      System.out
          .println("final working directory: " + jt.getWorkingDirectory());
      System.out.println("init error directory " + jt.getErrorPath());
      System.out.println("init out directory " + jt.getOutputPath());
      jt.setErrorPath(":" + home + "/jterror.txt");
      jt.setOutputPath(":" + home + "/jtout.txt");
      System.out.println("final error directory " + jt.getErrorPath());
      System.out.println("final out directory " + jt.getOutputPath());
      jt.setArgs(Lists
          .newArrayList(
              "-Djava.library.path=/opt/uge816/lib/lx-amd64/:/opt/ibm/ILOG/CPLEX_Studio125/cplex/bin/x86-64_sles10_4.1/",
              "-jar", home + "/drmaaTest.jar", "worker", "3")); // +
      // JobTemplate.PARAMETRIC_INDEX));
      String id = session.runJob(jt);

      System.out.println("Your job has been submitted with id " + id);

      session.deleteJobTemplate(jt);

      JobInfo info = session.wait(id, Session.TIMEOUT_WAIT_FOREVER);

      if (info.wasAborted()) {
        System.out.println("Job " + info.getJobId() + " never ran");
      } else if (info.hasExited()) {
        System.out.println("Job " + info.getJobId()
            + " finished regularly with exit status " + info.getExitStatus());
      } else if (info.hasSignaled()) {
        System.out.println("Job " + info.getJobId()
            + " finished due to signal " + info.getTerminatingSignal());
      } else {
        System.out.println("Job " + info.getJobId()
            + " finished with unclear conditions");
      }

      System.out.println("Job Usage:");

      session.exit();
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
      BufferedWriter writer = new BufferedWriter(new FileWriter("job" + jobId
          + ".txt"));
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
