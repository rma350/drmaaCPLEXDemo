drmaaCPLEXDemo
==============

Use DRMAA to create and solve many linear programs in parallel with sun grid compute engine, then collect and analyze the results.

Usage (master):

```bash
~/drmaaTest$ qrsh -cwd java -Djava.library.path=/opt/uge816/lib/lx-amd64/:/opt/ibm/ILOG/CPLEX_Studio125/cplex/bin/x86-64_sles10_4.1/ -jar drmaaTest.jar master 8
```

Usage (worker):

```bash
~/drmaaTest$ qrsh -cwd java -Djava.library.path=/opt/uge816/lib/lx-amd64/:/opt/ibm/ILOG/CPLEX_Studio125/cplex/bin/x86-64_sles10_4.1/ -jar drmaaTest.jar worker 6
```

The command ```bash qsub``` can be substituted for ```bash qrsh```.  This will start things in batch mode instead of interactive mode.  Read about the sun grid scheduler for more information.
