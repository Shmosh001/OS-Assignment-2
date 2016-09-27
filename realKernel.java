import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class realKernel implements Kernel
{

	List<realIODevice> ioDevices = new ArrayList<realIODevice>(); //List to store IO devices
	Queue<PCB> readyQ = new LinkedList<PCB>(); //Queue to store ready PCBs
	int pidCounter = 0; //used as a pcb ID counter
	realCPU cpu = new realCPU(); //CPU

	@Override
	//System Calls
	public int syscall(int number, Object... varargs)
	{
		//Creates a IO device
		if (number == SystemCall.MAKE_DEVICE)
		{
			sysCallTrace(number, varargs);
			realIODevice tempDevice = new realIODevice((int) varargs[0], varargs[1].toString());

			ioDevices.add(tempDevice);

		}

		//Reads and program and its instructions. Creates a PCB for the program
		else if (number == SystemCall.EXECVE)
		{
			sysCallTrace(number, varargs);
			PCB tempPCB = new PCB(varargs[0].toString(), pidCounter);
			pidCounter++;
			try
			{
				BufferedReader input = new BufferedReader(new FileReader(tempPCB.getProgramName()));
				String s = input.readLine();
				while (s != null)
				{
					if (!s.substring(0, 1).equals("#") && s.substring(0, 3).equals("CPU"))
					{
						int Duration = Integer.parseInt(s.substring(4));
						tempPCB.instructList.add(new CPUInstruction(Duration));

					}

					else if (!s.substring(0, 1).equals("#") && s.substring(0, 2).equals("IO"))
					{
						String[] items = s.split(" ");
						int Duration = Integer.parseInt(items[1]);
						int ID = Integer.parseInt(items[2]);
						tempPCB.instructList.add(new IOInstruction(Duration, ID));
					}

					s = input.readLine();
				}
				input.close();
				readyQ.add(tempPCB);

				if (cpu.isIdle() == true)
				{
					cpu.contextSwitch(readyQ.poll());

					//create timeout event
					simulatorDriver.sysTimer.scheduleInterrupt((int) simulatorDriver.sysTimer.getSystemTime()
							+ simulatorDriver.sysTimer.timeSlice + 2 + simulatorDriver.sysTimer.getOverhead(),
							(PCB) cpu.getCurrentProcess());
					simulatorDriver.sysTimer.advanceKernelTime((long) (simulatorDriver.sysTimer.getOverhead()));
				}

			}
			catch (Exception e)
			{
				// TODO: handle exception
				System.out.println("Error: " + e);
			}

		}

		//request to use IO device for a given time
		else if (number == SystemCall.IO_REQUEST)
		{
			sysCallTrace(number, varargs);
			int ioID = (int) varargs[0]; //IO divice ID
			int duration = (int) varargs[1]; //duration 

			PCB oldPCB = (PCB) cpu.contextSwitch(null);

			int DeviceID = 0;
			//search for IO device
			for (int i = 0; i < ioDevices.size(); i++)
			{
				if (ioDevices.get(i).getID() == ioID)
				{
					DeviceID = i;
					//add PCB to device queue
					ioDevices.get(i).addPCB(oldPCB);
					break;
				}
			}

			WakeUpEvent wakeUp;
			//if freeTime 
			if (ioDevices.get(DeviceID).getFreeTime() <= simulatorDriver.sysTimer.getSystemTime())
			{
				wakeUp = new WakeUpEvent(simulatorDriver.sysTimer.getSystemTime() + duration, ioDevices.get(DeviceID),
						oldPCB);
			}

			else
			{
				wakeUp = new WakeUpEvent(ioDevices.get(DeviceID).getFreeTime() + duration, ioDevices.get(DeviceID),
						oldPCB);
			}

			simulatorDriver.eventQ.add(wakeUp);
			simulatorDriver.sysTimer.advanceKernelTime(simulatorDriver.sysTimer.getOverhead());

		}

		//terminate process
		else if (number == SystemCall.TERMINATE_PROCESS)
		{

			sysCallTrace(number, varargs);
			//if ready queue isn't empty
			if (readyQ.isEmpty() == false)
			{
				PCB newPCB = readyQ.poll();
				//pull old process off cpu and put a new one on
				PCB oldPCB = (PCB) cpu.contextSwitch(newPCB);

				simulatorDriver.sysTimer.cancelInterrupt(oldPCB); //cancel time out interrupt
				//create new timeout event for new PCB on CPU
				simulatorDriver.sysTimer.scheduleInterrupt((int) simulatorDriver.sysTimer.getSystemTime()
						+ simulatorDriver.sysTimer.timeSlice + 2 + simulatorDriver.sysTimer.getOverhead(), newPCB);

				simulatorDriver.sysTimer.advanceKernelTime((long) (simulatorDriver.sysTimer.getOverhead()));

			}

			//ready queue empty
			else
			{
				//pull old process off cpu and put a new one on
				PCB oldPCB = (PCB) cpu.contextSwitch(null);
				simulatorDriver.sysTimer.cancelInterrupt(oldPCB); //cancel time out interrupt
				simulatorDriver.sysTimer.advanceKernelTime(simulatorDriver.sysTimer.getOverhead());

			}

		}
		// TODO Auto-generated method stub
		simulatorDriver.sysTimer.advanceKernelTime(2); //kernel time increments by 2
		System.out.printf("Time: %010d Kernel exit\n", simulatorDriver.sysTimer.getSystemTime());
		return 0;
	}

	@Override
	//Interrupts
	public void interrupt(int interruptType, Object... varargs)
	{
		interruptTrace(interruptType, varargs);
		//Time out interrupt
		if (interruptType == TIME_OUT)
		{

			if (readyQ.isEmpty() == false)
			{

				PCB oldPCB = (PCB) cpu.contextSwitch(readyQ.poll());//retrieve current process from cpu 

				if (oldPCB.getPID() != (int) varargs[0])
				{
					readyQ.add(oldPCB); //add current process to end of ready queue
				}

				simulatorDriver.sysTimer.scheduleInterrupt((int) simulatorDriver.sysTimer.getSystemTime()
						+ simulatorDriver.sysTimer.timeSlice + 2 + simulatorDriver.sysTimer.getOverhead(),
						(PCB) cpu.getCurrentProcess()); //create Time_Out event for new current process
				simulatorDriver.sysTimer.advanceKernelTime((long) (simulatorDriver.sysTimer.getOverhead()));

			}

			else
			{
				cpu.contextSwitch(cpu.getCurrentProcess());//retrieve current process from cpu
				simulatorDriver.sysTimer.scheduleInterrupt((int) simulatorDriver.sysTimer.getSystemTime()
						+ simulatorDriver.sysTimer.timeSlice + 2 + simulatorDriver.sysTimer.getOverhead(),
						(PCB) cpu.getCurrentProcess());

				simulatorDriver.sysTimer.advanceKernelTime((long) (simulatorDriver.sysTimer.getOverhead()));

			}

		}

		//Wake Up interrupt 
		if (interruptType == WAKE_UP)
		{
			int dID = (int) varargs[0];
			int pID = (int) varargs[1];

			//find position of device ID
			int DeviceID = 0;
			for (int i = 0; i < ioDevices.size(); i++)
			{
				if (ioDevices.get(i).getID() == dID)
				{
					DeviceID = i;
					//add PCB to device queue
					break;
				}
			}

			//removes pcb from IO device queue, increments pc counter and place pcb at the back of the ready queue
			for (ProcessControlBlock pcb : ioDevices.get(DeviceID).deviceQueue)
			{
				if (pcb.getPID() == pID)
				{
					ioDevices.get(DeviceID).deviceQueue.remove(pcb);
					pcb.nextInstruction();
					readyQ.add((PCB) pcb);
				}
			}

			cpu.contextSwitch(readyQ.poll());
			simulatorDriver.sysTimer.advanceKernelTime(2);

		}

		simulatorDriver.sysTimer.advanceKernelTime(2);

		System.out.printf("Time: %010d Kernel exit\n", simulatorDriver.sysTimer.getSystemTime());
	}

	private void sysCallTrace(int number, Object... varargs)
	{
		String details = null;
		switch (number)
		{
		case MAKE_DEVICE:
			details = String.format("MAKE_DEVICE, %s,\"%s\"", varargs[0], varargs[1]);
			break;
		case EXECVE:
			details = String.format("EXECVE, \"%s\"", varargs[0]);
			break;
		case IO_REQUEST:
			details = String.format("IO_REQUEST, %s, %s", varargs[0], varargs[1]);
			break;
		case TERMINATE_PROCESS:
			details = "TERMINATE_PROCESS";
			break;
		default:
			details = "ERROR_UNKNOWN_NUMBER";
		}
		System.out.printf("Time: %010d SysCall(%s)\n", simulatorDriver.sysTimer.getSystemTime(), details);
	}

	private void interruptTrace(int interruptType, Object... varargs)
	{
		String details = null;
		switch (interruptType)
		{
		case TIME_OUT:
			details = String.format("TIME_OUT, %s", varargs[0]);
			break;
		case WAKE_UP:
			details = String.format("WAKE_UP, %s, %s", varargs[0], varargs[1]);
			break;
		default:
			details = "ERROR_UNKNOWN_NUMBER";
		}
		System.out.printf("Time: %010d Interrupt(%s)\n", simulatorDriver.sysTimer.getSystemTime(), details);
	}
}
