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

The command ```qsub``` can be substituted for ```qrsh```.  This will start things in batch mode instead of interactive mode.  Read about the sun grid scheduler for more information.


The output will appear as follows:

```bash
:~/drmaaTest$ qrsh -cwd java -Djava.library.path=/opt/uge816/lib/lx-amd64/:/opt/ibm/ILOG/CPLEX_Studio125/cplex/bin/x86-64_sles10_4.1/ -jar drmaaTest.jar master 6
Your job has been submitted with id 223779
Your job has been submitted with id 223780
Your job has been submitted with id 223781
Your job has been submitted with id 223782
Your job has been submitted with id 223783
Your job has been submitted with id 223784
All jobs finished!
waiting for file system to show job0.txt.  Sleeping 2000ms
waiting for file system to show job0.txt.  Sleeping 4000ms
waiting for file system to show job0.txt.  Sleeping 8000ms
waiting for file system to show job0.txt.  Sleeping 16000ms
waiting for file system to show job0.txt.  Sleeping 32000ms
Answers: [1.0, 3.0, 5.0, 7.0, 9.0, 11.0]
```
