Author: Osher Shuman
Student No: SHMOSH001
Due Date: 25/3/2015

Operating Systems Assignemnt 2

The simulatorDriver is the driver of the program which will run first and create a system timer as well as a kernel and events. 
The simulatorDriver receives 3 arguments. The config filepath, time slice quantum and context switch overhead.
The cpu is created in the kernel and executes processes(PCBs). The kernel has system calls and interrupts that are called.
The following classes are implementations I made: realCPU, realIODevice, realKernel, realSystemTimer and PCB. PCB stores processes with instructions as a process control block.



