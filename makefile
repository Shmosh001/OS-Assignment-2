#Makefile

JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = simulatorDriver.java CPU.java CPUInstruction.java Event.java EventQueue.java ExecveEvent.java Instruction.java InterruptHandler.java IODevice.java IOInstruction.java Kernel.java ProcessControlBlock.java realCPU.java realIODevice.java realKernel.java PCB.java realSystemTimer.java SystemCall.java SystemTimer.java TimeOutEvent.java WakeUpEvent.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class