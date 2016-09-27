import java.io.BufferedReader;
import java.io.FileReader;

public class simulatorDriver
{
	public static EventQueue eventQ = new EventQueue();
	public static realSystemTimer sysTimer = new realSystemTimer();
	int timeSlice;
	int overhead;

	public simulatorDriver(String fp, int sl, int doh)
	{
		String filepath = fp;
		timeSlice = sl;
		overhead = doh;
		realKernel kernel = new realKernel();
		sysTimer.setTimeSlice(timeSlice);
		sysTimer.setOverhead(overhead);

		try
		{
			//read config file
			BufferedReader inputConf = new BufferedReader(new FileReader(filepath));
			String s = inputConf.readLine();
			while (s != null)
			{
				//check if line is a comment
				if (!(s.substring(0, 1).equals("#")))
				{
					String[] conf = s.split(" ");

					//IO device
					if (conf[0].equals("DEVICE"))
					{
						//create IO device
						kernel.syscall(kernel.MAKE_DEVICE, Integer.parseInt(conf[1]), conf[2]);
					}

					//Program
					else if (conf[0].equals("PROGRAM"))
					{
						long arrivalTime = Long.parseLong(conf[1]);
						String programName = conf[2];
						//create new program event
						Event e = new ExecveEvent(arrivalTime, programName);
						eventQ.add(e); //add event
					}
				}
				s = inputConf.readLine();
			}
			inputConf.close();

			//reset system timer and reset slice and overhead values
			sysTimer = new realSystemTimer(); //reset system time to 0
			sysTimer.setTimeSlice(timeSlice);
			sysTimer.setOverhead(overhead);

			//main event loop
			//check that both event queue and ready queue are not empty
			while (eventQ.isEmpty() == false || kernel.readyQ.isEmpty() == false)
			{

				if (eventQ.isEmpty() == false)
				{
					Event temp = eventQ.poll(); //obtain event off event queue

					long arrivalTime = temp.getTime();

					//event has arrived
					if (arrivalTime <= sysTimer.getSystemTime())
					{

						kernel.cpu.getKernel(kernel); //send kernel to CPU
						temp.eventProcess(kernel); //process event

						Event tempNext = eventQ.peek();

						int timeRemain = 0;
						if (tempNext != null && tempNext.getTime() <= sysTimer.getSystemTime())
						{
							eventQ.poll();
							tempNext.eventProcess(kernel); //process event
							timeRemain = kernel.cpu.execute(sysTimer.timeSlice - 2); //execute process
						}

						else if (tempNext != null && tempNext.getTime() > sysTimer.getSystemTime())
						{
							timeRemain = kernel.cpu.execute(sysTimer.timeSlice); //execute process
						}

						if (timeRemain == 0)
						{
							ProcessControlBlock tempPCB = (PCB) kernel.cpu.getCurrentProcess();
							if (tempPCB.getState() == ProcessControlBlock.State.READY)
							{
								kernel.readyQ.add((PCB) tempPCB);
							}

						}

					}

					//event still has to happen
					else if (temp.getTime() > sysTimer.getSystemTime())
					{
						//CPU is not idle
						if (kernel.cpu.isIdle() == false)
						{
							kernel.cpu.execute(sysTimer.timeSlice);
						}

						//CPU is idle
						else
						{
							//advance idle time 
							sysTimer.advanceIdleTime(arrivalTime - sysTimer.sysTime);
							eventQ.add(temp);
						}

					}
				}

			}

			//rounding CPU Utilization time to 2 decimal places
			double cpuUtil = (double) sysTimer.getUserTime() / (double) sysTimer.getSystemTime();
			cpuUtil = cpuUtil * 10000;
			cpuUtil = Math.round(cpuUtil);
			cpuUtil = cpuUtil / 100;

			//print out times
			System.out.println("System time: " + sysTimer.getSystemTime());
			System.out.println("Kernel time: " + sysTimer.getKernelTime());
			System.out.println("User time: " + sysTimer.getUserTime());
			System.out.println("Idle time: " + sysTimer.getIdleTime());
			System.out.println("Context switches: " + kernel.cpu.getCS());
			System.out.println("CPU utilization: " + cpuUtil);

		}
		catch (Exception e)
		{
			// TODO: handle exception
			System.out.println("Error: " + e);
		}
	}

	public static void main(String[] args)
	{
		String configPath = args[0]; //config path
		int slice = Integer.parseInt(args[1]); //slice time quantum
		int dispatchOverhead = Integer.parseInt(args[2]);
		simulatorDriver driver = new simulatorDriver(configPath, slice, dispatchOverhead);

	}
}
