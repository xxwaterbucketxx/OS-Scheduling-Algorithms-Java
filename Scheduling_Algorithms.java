//Name: Victor Vu   NetID: vhv180000
//Project 03: Scheduling Algorithms
//This project was used to demonstrate the understanding of 5 different
// scheduling algorithms that include First Come First Served, Round Robin,
// Shortest Process Next, Highest Response Ratio Next, and Feedback.
//
// The arguments this program takes are within a command line where the
// first argument is the schedule file and the second argument is the
// program file with a certain format that is given with the given files

import java.util.*;
import java.io.*;

public class Scheduling_Algorithms
{
    public static void main(String[] args)
    {
        Scanner scheduler,
                program;

        if (args.length != 2)
        {
            System.out.println("There are not enough arguments");
            return;
        }
        try
        {
            scheduler = new Scanner(new File(args[0]));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("The file " + args[0] + " cannot be found within this directory");
            System.out.println("The program will now exit");
            return;
        }
        try
        {
            program = new Scanner(new File(args[1]));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("The file " + args[1] + " cannot be found within this directory");
            System.out.println("The program will now exit");
            return;
        }

        String scheduler_choice,
               event = null;

        int number,
            ID = 0,
            CurrentTime = 0,
            EventTime = 0,
            ExpectTime = 0,
            ServiceTime = 0,
            ResponseTime_cal = 0;

        scheduler_choice = scheduler.nextLine();
        process CPU = null;

        ArrayList<Event> PriorityEvent = new ArrayList<>();

        ArrayList<process> New = new ArrayList<>();
        ArrayList<process> Ready = new ArrayList<>();
        ArrayList<process> Blocked = new ArrayList<>();
        ArrayList<process> Exit = new ArrayList<>();
        ArrayList<process> Auxiliary = new ArrayList<>();
        String line;

        int delta;

        //This is used to process the program file and to fill the event priority queue up with arrivals from the file
        while (program.hasNextLine())
        {
            process NewProcess = new process();
            NewProcess.setLine(program.nextLine());

            line = NewProcess.getLine();

            number = Integer.parseInt(line.substring(0,line.indexOf(' ')));
            NewProcess.setArrive(number);

            line = line.substring(line.indexOf(' ') + 1);
            NewProcess.setLine(line);

            NewProcess.setID(ID);
            ID++;

            Event NewEvent = new Event();
            NewEvent.setEvent("Arrive");
            NewEvent.setStamp(NewProcess.getArrive());
            NewEvent.setPID(NewProcess);

            PriorityEvent.add(NewEvent);

            //This is an overridden method to allow organization of the time stamps
            PriorityEvent.sort(new eventComparator());

            New.add(NewProcess);
        }
        program.close();

        //This is used for processing the program file with the First Come First Serve algorithm
        if (scheduler_choice.equals("FCFS"))
        {
            while (true)
            {
                //This tells us if there is nothing in the event queue then we need to exit the algorithm
                if (PriorityEvent.size() == 0)
                    break;

                //This is used to ensure the event that was scheduled must be present to do any processing
                if (CurrentTime == PriorityEvent.get(0).getStamp())
                {
                    //This is used to check if the process on the CPU needs either Exit or get Unblocked
                    if (CPU != null && CPU.getEventTime() == -1)
                    {
                        CPU.setEventTime(0);
                        CPU.nextEvent();

                        //This is the exit routine for processes that are ready to terminate
                        if (CPU.getLine().equals(""))
                        {
                            Process_Exit_Routine(CPU, CurrentTime);
                            Exit.add(CPU);
                            CPU = null;

                            PriorityEvent.remove(0);
                        }

                        //This is the blocking routine for process that need
                        // to be blocked since their CPU activity has ended
                        if (CPU != null && CPU.getEvent().equals("IO"))
                        {
                            Blocked.add(CPU);
                            CPU.getEventTime();

                            //This is used to generate the Unblock event since it was just blocked above
                            Event NewEvent = new Event("UNBLOCK", CurrentTime + CPU.getEventTime(), CPU);

                            PriorityEvent.add(NewEvent);
                            PriorityEvent.sort(new eventComparator());
                            CPU = null;
                            PriorityEvent.remove(0);
                        }
                    }

                    //This is another early program termination section just in case
                    // the last event was knocked off in the previous section
                    if (New.size() == 0 && Ready.size() == 0 && Blocked.size() == 0 && CPU == null)
                        break;

                    //This is the arrival routine the takes care of the arrival events in the event
                    // queue and puts processes from the new state into the ready state when they arrive
                    if (New.size() != 0 && New.get(0).getArrive() == CurrentTime)
                    {
                        Ready.add(New.get(0));
                        New.get(0).getResponse().add(CurrentTime);

                        New.remove(0);
                        PriorityEvent.remove(0);
                    }

                    //This is the unblock routine that takes care of unblock events in the event
                    // queue and puts process from the block state into the ready state when it is their time
                    if (Blocked.size() != 0)
                    {
                        for (int x = 0; x < Blocked.size(); x++)
                        {
                            if (Blocked.get(x).getEventTime() == -1)
                            {
                                Ready.add(Blocked.get(x));
                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();
                                Blocked.get(x).getResponse().add(CurrentTime);
                                Blocked.set(x, null);
                                PriorityEvent.remove(0);
                            }
                        }

                        while (Blocked.remove(null));
                    }

                    //This is the CPU routine that takes care of assigning process onto the CPU
                    // but also generates a Block or Exit event in the case that either the process
                    // has more to process or is on its last CPU call respectively
                    if (CPU == null && Ready.size() != 0)
                    {
                        CPU = Ready.get(0);
                        Ready.remove(0);
                        CPU.getResponse().add(CurrentTime);

                        CPU.getEvent();
                        CPU.getEventTime();

                        if (CPU.getStart() == -1)
                            CPU.setStart(CurrentTime);

                        Event NewEvent = new Event();

                        if (CPU.getNumEvent() >= 2)
                            NewEvent.setEvent("BLOCK");
                        else if (CPU.getNumEvent() == 1)
                            NewEvent.setEvent("EXIT");

                        NewEvent.setStamp(CPU.getEventTime() + CurrentTime);
                        NewEvent.setPID(CPU);

                        PriorityEvent.add(NewEvent);
                        PriorityEvent.sort(new eventComparator());
                    }
                }

                //This is the time routine that takes care of when to increment the time
                // based on the event queue as well as incrementing the services times of
                // processes and decrementing the amount of time the process has left on the CPU
                if (CurrentTime < PriorityEvent.get(0).getStamp())
                {
                    delta = PriorityEvent.get(0).getStamp() - CurrentTime;

                    CurrentTime = PriorityEvent.get(0).getStamp();

                    //This is the service time section
                    if (CPU != null)
                    {
                        CPU.setService(CPU.getService() + delta);
                        if (CPU.getEventTime() == delta)
                            CPU.setEventTime(-1);
                        else
                            CPU.setEventTime(CPU.getEventTime() - delta);
                    }

                    //This is the decrement time section for IO activities
                    if (Blocked.size() != 0)
                    {
                        for (process p : Blocked)
                        {
                            if (p.getEventTime() == delta)
                                p.setEventTime(-1);
                            else
                                p.setEventTime(p.getEventTime() - delta);
                        }
                    }
                }
            }
        }

        //This is used for processing the program file with the Round Robin algorithm
        else if (scheduler_choice.equals("RR"))
        {
            //This is used to get the time quantum inside the schedule file
            line = scheduler.nextLine();
            int TimeQuantum = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            int runningQuantum = 0;
            scheduler.close();

            while (true)
            {
                //This tells us if there is nothing in the event queue then we need to exit the algorithm
                if (PriorityEvent.size() == 0)
                    break;

                //This is used to ensure the event that was scheduled must be present to do any processing
                if (CurrentTime == PriorityEvent.get(0).getStamp())
                {
                    //This is used to check if the process on the CPU needs either to Exit or get Unblocked or Timed Out
                    if (CPU != null && (CPU.getEventTime() == -1 || runningQuantum == TimeQuantum))
                    {
                        if (CPU.getEventTime() == -1)
                        {
                            CPU.setEventTime(0);
                            CPU.nextEvent();

                            //This is the exit routine for processes that are ready to terminate
                            if (CPU.getLine().equals(""))
                            {
                                Process_Exit_Routine(CPU, CurrentTime);
                                Exit.add(CPU);
                                CPU = null;

                                PriorityEvent.remove(0);
                            }

                            //This is the blocking routine for process that need
                            // to be blocked since their CPU activity has ended
                            if (CPU != null && CPU.getEvent().equals("IO"))
                            {
                                Blocked.add(CPU);
                                CPU.getEventTime();

                                //This is used to generate the Unblock event since it was just blocked above
                                Event NewEvent = new Event("UNBLOCK", CurrentTime + CPU.getEventTime(), CPU);

                                PriorityEvent.add(NewEvent);
                                PriorityEvent.sort(new eventComparator());
                                CPU = null;
                                PriorityEvent.remove(0);
                            }
                        }
                        //This is for if the process needs to be timed out
                        else if (runningQuantum == TimeQuantum)
                        {
                            Auxiliary.add(CPU);
                            CPU = null;

                            PriorityEvent.remove(0);
                        }

                        runningQuantum = 0;
                    }

                    //This is another early program termination section just in case
                    // the last event was knocked off in the previous section
                    if (New.size() == 0 && Ready.size() == 0 && Blocked.size() == 0 && Auxiliary.size() == 0 && CPU == null)
                        break;

                    //This is the arrival routine the takes care of the arrival events in the event
                    // queue and puts processes from the new state into the ready state when they arrive
                    if (New.size() != 0 && New.get(0).getArrive() == CurrentTime)
                    {
                        Ready.add(New.get(0));
                        New.get(0).getResponse().add(CurrentTime);

                        New.remove(0);
                        PriorityEvent.remove(0);
                    }

                    //This is the Auxiliary routine that takes care of timeout events in the event
                    // queue and puts the process from auxiliary queue into the ready queue
                    if (Auxiliary.size() != 0)
                    {
                        for (int x = 0; x < Auxiliary.size(); x++)
                        {
                            Ready.add(Auxiliary.get(x));
                            Auxiliary.get(x).getResponse().add(CurrentTime);
                            Auxiliary.set(x, null);
                        }

                        while (Auxiliary.remove(null));
                    }

                    //This is the unblock routine that takes care of unblock events in the event
                    // queue and puts process from the block state into the ready state when it is their time
                    if (Blocked.size() != 0)
                    {
                        for (int x = 0; x < Blocked.size(); x++)
                        {
                            if (Blocked.get(x).getEventTime() == -1 && Blocked.get(x).getNumEvent() != 1)
                            {
                                Ready.add(Blocked.get(x));
                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();
                                Blocked.get(x).getResponse().add(CurrentTime);
                                Blocked.set(x, null);

                                PriorityEvent.remove(0);
                            }
                            //This is a case just in the matter that there is a I/O call at the end of a process, it enters then immediately puts it into an exit routine
                            else if (Blocked.get(x).getEventTime() == -1 && Blocked.get(x).getNumEvent() == 1 && Blocked.get(0).getEvent().equals("IO"))
                            {
                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();

                                if (Blocked.get(x).getLine().equals(""))
                                {
                                    Process_Exit_Routine(Blocked.get(x), CurrentTime);
                                    Exit.add(Blocked.get(x));
                                    Blocked.set(x, null);

                                    PriorityEvent.remove(0);
                                }
                            }
                        }

                        while (Blocked.remove(null));
                    }

                    //This is the CPU routine that takes care of assigning process onto the CPU
                    // but also generates a Block or Exit event in the case that either the process
                    // has more to process or is on its last CPU call respectively or a Timeout event
                    // in the matter that the duration of the CPU activity is larger than the time quantum
                    if (CPU == null && Ready.size() != 0)
                    {
                        CPU = Ready.get(0);
                        Ready.remove(0);
                        CPU.getResponse().add(CurrentTime);

                        CPU.getEvent();
                        CPU.getEventTime();

                        if (CPU.getStart() == -1)
                            CPU.setStart(CurrentTime);

                        Event NewEvent = new Event();

                        if (CPU.getNumEvent() == 1 && CPU.getEventTime() <= TimeQuantum)
                        {
                            NewEvent.setEvent("EXIT");
                            NewEvent.setStamp(CurrentTime + CPU.getEventTime());
                        }
                        else if (CPU.getEventTime() > TimeQuantum)
                        {
                            NewEvent.setEvent("TIMEOUT");
                            NewEvent.setStamp(CurrentTime + TimeQuantum);
                        }
                        else if (CPU.getEventTime() <= 5)
                        {
                            NewEvent.setEvent("BLOCK");
                            NewEvent.setStamp(CurrentTime + CPU.getEventTime());
                        }

                        NewEvent.setPID(CPU);

                        PriorityEvent.add(NewEvent);
                        PriorityEvent.sort(new eventComparator());
                    }
                }

                //This is the time routine that takes care of when to increment the time
                // based on the event queue as well as incrementing the services times of
                // processes and decrementing the amount of time the process has left on the CPU
                if (CurrentTime < PriorityEvent.get(0).getStamp())
                {
                    delta = PriorityEvent.get(0).getStamp() - CurrentTime;

                    CurrentTime += delta;

                    //This is the service time section
                    if (CPU != null)
                    {
                        runningQuantum += delta;

                        CPU.setService(CPU.getService() + delta);
                        if (CPU.getEventTime() == delta)
                            CPU.setEventTime(-1);
                        else
                            CPU.setEventTime(CPU.getEventTime() - delta);
                    }

                    //This is the decrement time section for IO activities
                    if (Blocked.size() != 0)
                    {
                        for (process p : Blocked)
                        {
                            if (p.getEventTime() == delta)
                                p.setEventTime(-1);
                            else
                                p.setEventTime(p.getEventTime() - delta);
                        }
                    }
                }
            }
        }
        //This is used for processing the program file with the Shortest Process Next algorithm
        else if (scheduler_choice.equals("SPN"))
        {
            //This is used to get whether we are able to know the exact services
            // times of each process to then do exponential averaging accordingly
            boolean given;
            line = scheduler.nextLine();
            given = Boolean.parseBoolean(line.substring(line.indexOf('=') + 1));

            while (true)
            {
                //This tells us if there is nothing in the event queue then we need to exit the algorithm
                if (PriorityEvent.size() == 0)
                    break;

                //This is used to ensure the event that was scheduled must be present to do any processing
                if (CurrentTime == PriorityEvent.get(0).getStamp())
                {
                    //This is used to check if the process on the CPU needs either to Exit or get Unblocked or Timed Out
                    if (CPU != null && CPU.getEventTime() == -1)
                    {
                        CPU.setEventTime(0);
                        CPU.nextEvent();

                        //This is the exit routine for processes that are ready to terminate
                        if (CPU.getLine().equals(""))
                        {
                            Process_Exit_Routine(CPU, CurrentTime);
                            Exit.add(CPU);
                            CPU = null;

                            PriorityEvent.remove(0);
                        }

                        //This is the blocking routine for process that need
                        // to be blocked since their CPU activity has ended
                        if (CPU != null && CPU.getEvent().equals("IO"))
                        {
                            Blocked.add(CPU);
                            CPU.getEventTime();

                            //This is used to generate the Unblock event since it was just blocked above
                            Event NewEvent = new Event("UNBLOCK", CurrentTime + CPU.getEventTime(), CPU);

                            PriorityEvent.add(NewEvent);
                            PriorityEvent.sort(new eventComparator());
                            CPU = null;
                            PriorityEvent.remove(0);
                        }
                    }

                    //This is another early program termination section just in case
                    // the last event was knocked off in the previous section
                    if (New.size() == 0 && Ready.size() == 0 && Blocked.size() == 0 && CPU == null)
                        break;

                    //This is the arrival routine the takes care of the arrival events in the event
                    // queue and puts processes from the new state into the ready state when they arrive
                    if (New.size() != 0 && New.get(0).getArrive() == CurrentTime)
                    {
                        Ready.add(New.get(0));
                        New.get(0).getResponse().add(CurrentTime);

                        //This sets the SP value in the process for later use
                        New.get(0).setSP(New.get(0).getEventTime());

                        New.remove(0);
                        PriorityEvent.remove(0);

                    }

                    //This is the unblock routine that takes care of unblock events in the event
                    // queue and puts process from the block state into the ready state when it is their time
                    if (Blocked.size() != 0)
                    {
                        for (int x = 0; x < Blocked.size(); x++)
                        {
                            if (Blocked.get(x).getEventTime() == -1)
                            {
                                Ready.add(Blocked.get(x));
                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();
                                Blocked.get(x).getResponse().add(CurrentTime);
                                Blocked.get(x).setSP(Blocked.get(x).getEventTime());
                                Blocked.set(x, null);

                                PriorityEvent.remove(0);
                            }
                        }

                        while (Blocked.remove(null));
                    }

                    //This is the CPU routine that takes care of assigning process onto the CPU
                    // but with the selection power of looking to see what is the shortest process to
                    // put on the CPU next with either the actual CPU service time or an estimate with
                    // exponential averaging  also generates a Block or Exit event in the case that
                    // either the process has more to process or is on its last CPU call respectively
                    process shortest = new process();
                    if (CPU == null && Ready.size() != 0)
                    {
                        //This is the selection routine if the file says true
                        if (given)
                        {
                            shortest = Ready.get(0);
                            for (process p : Ready)
                            {
                                if (shortest.getEventTime() > p.getEventTime())
                                    shortest = p;
                            }
                        }
                        //This is the selction routine if the file says false
                        else
                        {
                            shortest = Ready.get(0);
                            for (process p : Ready)
                            {
                                if (shortest.getSP() > p.getSP())
                                    shortest = p;
                            }
                        }

                        CPU = shortest;
                        Ready.remove(shortest);
                        CPU.getResponse().add(CurrentTime);

                        CPU.getEvent();
                        CPU.getEventTime();

                        if (CPU.getStart() == -1)
                            CPU.setStart(CurrentTime);

                        Event NewEvent = new Event();

                        if (CPU.getNumEvent() >= 2)
                            NewEvent.setEvent("BLOCK");
                        else if (CPU.getNumEvent() == 1)
                            NewEvent.setEvent("EXIT");

                        NewEvent.setStamp(CPU.getEventTime() + CurrentTime);
                        NewEvent.setPID(CPU);

                        PriorityEvent.add(NewEvent);
                        PriorityEvent.sort(new eventComparator());
                    }
                }

                //This is the time routine that takes care of when to increment the time
                // based on the event queue as well as incrementing the services times of
                // processes and decrementing the amount of time the process has left on the CPU
                if (CurrentTime < PriorityEvent.get(0).getStamp())
                {
                    delta = PriorityEvent.get(0).getStamp() - CurrentTime;

                    CurrentTime += delta;

                    if (CPU != null)
                    {
                        CPU.setService(CPU.getService() + delta);
                        if (CPU.getEventTime() == delta)
                            CPU.setEventTime(-1);
                        else
                            CPU.setEventTime(CPU.getEventTime() - delta);
                    }

                    if (Blocked.size() != 0)
                    {
                        for (process p : Blocked)
                        {
                            if (p.getEventTime() == delta)
                                p.setEventTime(-1);
                            else
                                p.setEventTime(p.getEventTime() - delta);
                        }
                    }
                }
            }
        }
        //This is used for processing the program file with the Highest Response Ratio Next algorithm
        else if (scheduler_choice.equals("HRRN"))
        {
            //This is used to get whether we are able to know the exact services
            // times of each process to then do exponential averaging accordingly
            boolean given;
            line = scheduler.nextLine();
            given = Boolean.parseBoolean(line.substring(line.indexOf('=') + 1));

            while (true)
            {
                //This tells us if there is nothing in the event queue then we need to exit the algorithm
                if (PriorityEvent.size() == 0)
                    break;

                //This is used to ensure the event that was scheduled must be present to do any processing
                if (CurrentTime == PriorityEvent.get(0).getStamp())
                {
                    //This is used to check if the process on the CPU needs either to Exit or get Unblocked or Timed Out
                    if (CPU != null && CPU.getEventTime() == -1)
                    {
                        CPU.setEventTime(0);
                        CPU.nextEvent();

                        //This is the exit routine for processes that are ready to terminate
                        if (CPU.getLine().equals(""))
                        {
                            Process_Exit_Routine(CPU, CurrentTime);
                            Exit.add(CPU);
                            CPU = null;

                            PriorityEvent.remove(0);
                        }

                        //This is the blocking routine for process that need
                        // to be blocked since their CPU activity has ended
                        if (CPU != null && CPU.getEvent().equals("IO"))
                        {
                            Blocked.add(CPU);
                            CPU.getEventTime();

                            //This is used to generate the Unblock event since it was just blocked above
                            Event NewEvent = new Event("UNBLOCK", CurrentTime + CPU.getEventTime(), CPU);

                            PriorityEvent.add(NewEvent);
                            PriorityEvent.sort(new eventComparator());
                            CPU = null;
                            PriorityEvent.remove(0);
                        }
                    }

                    //This is another early program termination section just in case
                    // the last event was knocked off in the previous section
                    if (New.size() == 0 && Ready.size() == 0 && Blocked.size() == 0 && CPU == null)
                        break;

                    //This is the arrival routine the takes care of the arrival events in the event
                    // queue and puts processes from the new state into the ready state when they arrive
                    if (New.size() != 0 && New.get(0).getArrive() == CurrentTime)
                    {
                        Ready.add(New.get(0));
                        New.get(0).getResponse().add(CurrentTime);

                        //This sets the SP value in the process for later use
                        New.get(0).setSP(New.get(0).getEventTime());

                        New.remove(0);
                        PriorityEvent.remove(0);
                    }

                    //This is the unblock routine that takes care of unblock events in the event
                    // queue and puts process from the block state into the ready state when it is their time
                    if (Blocked.size() != 0)
                    {
                        for (int x = 0; x < Blocked.size(); x++)
                        {
                            if (Blocked.get(x).getEventTime() == -1)
                            {
                                Ready.add(Blocked.get(x));
                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();
                                Blocked.get(x).getResponse().add(CurrentTime);
                                Blocked.get(x).setSP(Blocked.get(x).getEventTime());
                                Blocked.set(x, null);

                                PriorityEvent.remove(0);
                            }
                        }

                        while (Blocked.remove(null));
                    }

                    //This is the CPU routine that takes care of assigning process onto the CPU
                    // but with the selection power of looking to see what is the highest response ratio to
                    // put on the CPU next with either the actual CPU service time or an estimate with
                    // exponential averaging also generates a Block or Exit event in the case that
                    // either the process has more to process or is on its last CPU call respectively
                    process highest = new process();
                    if (CPU == null && Ready.size() != 0)
                    {
                        highest = Ready.get(0);
                        double highestService,
                               ProspectService;

                        double highestRatio,
                               ProspectRatio;

                        //This is the selection routine if the file says true
                        if (given)
                        {
                            for (process p : Ready)
                            {
                                highestService = (CurrentTime - highest.getResponse().get(highest.getResponse().size() - 1));
                                ProspectService = (CurrentTime - p.getResponse().get(p.getResponse().size() - 1));

                                highestRatio = (highest.getEventTime() + highestService) / highest.getEventTime();
                                ProspectRatio = (p.getEventTime() + ProspectService) / p.getEventTime();

                                if (highestRatio < ProspectRatio)
                                    highest = p;
                            }
                        }
                        //This is the selction routine if the file says false
                        else
                        {
                            for (process p : Ready)
                            {
                                highestService = (CurrentTime - highest.getResponse().get(highest.getResponse().size() - 1));
                                ProspectService = (CurrentTime - p.getResponse().get(p.getResponse().size() - 1));

                                highestRatio = (highest.getSP() + highestService) / highest.getSP();
                                ProspectRatio = (p.getSP() + ProspectService) / p.getSP();

                                if (highestRatio < ProspectRatio)
                                    highest = p;
                            }
                        }

                        CPU = highest;
                        Ready.remove(highest);
                        CPU.getResponse().add(CurrentTime);

                        CPU.getEvent();
                        CPU.getEventTime();

                        if (CPU.getStart() == -1)
                            CPU.setStart(CurrentTime);

                        Event NewEvent = new Event();

                        if (CPU.getNumEvent() >= 2)
                            NewEvent.setEvent("BLOCK");
                        else if (CPU.getNumEvent() == 1)
                            NewEvent.setEvent("EXIT");

                        NewEvent.setStamp(CPU.getEventTime() + CurrentTime);
                        NewEvent.setPID(CPU);

                        PriorityEvent.add(NewEvent);
                        PriorityEvent.sort(new eventComparator());
                    }
                }

                //This is the time routine that takes care of when to increment the time
                // based on the event queue as well as incrementing the services times of
                // processes and decrementing the amount of time the process has left on the CPU
                if (CurrentTime < PriorityEvent.get(0).getStamp())
                {
                    delta = PriorityEvent.get(0).getStamp() - CurrentTime;

                    CurrentTime += delta;

                    if (CPU != null)
                    {
                        CPU.setService(CPU.getService() + delta);
                        if (CPU.getEventTime() == delta)
                            CPU.setEventTime(-1);
                        else
                            CPU.setEventTime(CPU.getEventTime() - delta);
                    }

                    if (Blocked.size() != 0)
                    {
                        for (process p : Blocked)
                        {
                            if (p.getEventTime() == delta)
                                p.setEventTime(-1);
                            else
                                p.setEventTime(p.getEventTime() - delta);
                        }
                    }
                }
            }
        }
        //This is used for processing the program file with the Highest Response Ratio Next algorithm
        else if (scheduler_choice.equals("FEEDBACK"))
        {
            //This is used to get both the time quantum and the number of ready queues needed in the algorithm
            line = scheduler.nextLine();
            int TimeQuantum = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            int runningQuantum = 0;

            line = scheduler.nextLine();
            int prioCount = Integer.parseInt(line.substring(line.indexOf('=') + 1));

            scheduler.close();

            ArrayList<ArrayList<process>> prioReady = new ArrayList<>();

            //This is used to create the number of ready queues as said from the file
            for (int x = 0; x < prioCount; x++)
            {
                ArrayList<process> list = new ArrayList<>();
                prioReady.add(list);
            }

            while (true)
            {
                //This tells us if there is nothing in the event queue then we need to exit the algorithm
                if (PriorityEvent.size() == 0)
                    break;

                //This is used to ensure the event that was scheduled must be present to do any processing
                if (CurrentTime == PriorityEvent.get(0).getStamp())
                {
                    //This is used to check if the process on the CPU needs either to Exit or get Unblocked or Timed Out
                    if (CPU != null && (CPU.getEventTime() == -1 || runningQuantum == TimeQuantum))
                    {
                        if (CPU.getEventTime() == -1)
                        {
                            CPU.setEventTime(0);
                            CPU.nextEvent();

                            //This is the exit routine for processes that are ready to terminate
                            if (CPU.getLine().equals(""))
                            {
                                Process_Exit_Routine(CPU, CurrentTime);
                                Exit.add(CPU);
                                CPU = null;

                                PriorityEvent.remove(0);
                            }

                            //This is the blocking routine for process that need
                            // to be blocked since their CPU activity has ended
                            if (CPU != null && CPU.getEvent().equals("IO"))
                            {
                                Blocked.add(CPU);
                                CPU.getEventTime();

                                Event NewEvent = new Event("UNBLOCK", CurrentTime + CPU.getEventTime(), CPU);

                                PriorityEvent.add(NewEvent);
                                PriorityEvent.sort(new eventComparator());
                                CPU = null;
                                PriorityEvent.remove(0);
                            }
                        }
                        //This is for if the process needs to be timed out
                        else if (runningQuantum == TimeQuantum)
                        {
                            Auxiliary.add(CPU);
                            CPU = null;

                            PriorityEvent.remove(0);
                        }

                        runningQuantum = 0;
                    }

                    //This is another early program termination section just in case
                    // the last event was knocked off in the previous section
                    if (New.size() == 0 && Ready.size() == 0 && Blocked.size() == 0 && Auxiliary.size() == 0 && CPU == null)
                    {
                        int runner = 0;
                        for (ArrayList<process> p: prioReady)
                        {
                            runner += p.size();
                        }

                        if (runner == 0)
                            break;
                    }

                    //This is the arrival routine the takes care of the arrival events in the event
                    // queue and puts processes from the new state into the ready state when they arrive
                    if (New.size() != 0 && New.get(0).getArrive() == CurrentTime)
                    {
                        prioReady.get(0).add(New.get(0));
                        New.get(0).getResponse().add(CurrentTime);

                        New.remove(0);
                        PriorityEvent.remove(0);
                    }

                    //This is the Auxiliary routine that takes care of timeout events in the event
                    // queue and puts the process from auxiliary queue into the ready queue
                    if (Auxiliary.size() != 0)
                    {
                        for (int x = 0; x < Auxiliary.size(); x++)
                        {
                            int y = Auxiliary.get(x).getPrioNum();

                            if (y == prioCount - 1)
                                prioReady.get(y).add(Auxiliary.get(x));
                            else
                                prioReady.get(y + 1).add(Auxiliary.get(x));

                            Auxiliary.get(x).getResponse().add(CurrentTime);
                            Auxiliary.set(x, null);
                        }

                        while (Auxiliary.remove(null));
                    }

                    //This is the unblock routine that takes care of unblock events in the event
                    // queue and puts process from the block state into the ready state when it is their time
                    if (Blocked.size() != 0)
                    {
                        for (int x = 0; x < Blocked.size(); x++)
                        {
                            if (Blocked.get(x).getEventTime() == -1 && Blocked.get(x).getNumEvent() != 1)
                            {
                                int y = Blocked.get(x).getPrioNum();

                                prioReady.get(y).add(Blocked.get(x));

                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();
                                Blocked.get(x).getResponse().add(CurrentTime);
                                Blocked.set(x, null);

                                PriorityEvent.remove(0);
                            }
                            //This is a case just in the matter that there is a I/O call at the end of a process, it enters then immediately puts it into an exit routine
                            else if (Blocked.get(x).getEventTime() == -1 && Blocked.get(x).getNumEvent() == 1 && Blocked.get(0).getEvent().equals("IO"))
                            {
                                Blocked.get(x).setEventTime(0);
                                Blocked.get(x).nextEvent();

                                if (Blocked.get(x).getLine().equals(""))
                                {
                                    Process_Exit_Routine(Blocked.get(x), CurrentTime);
                                    Exit.add(Blocked.get(x));
                                    Blocked.set(x, null);

                                    PriorityEvent.remove(0);
                                }
                            }
                        }

                        while (Blocked.remove(null));
                    }

                    //This is the CPU routine that takes care of assigning process onto the CPU with
                    // a priority selection out of a number of first come first serve queues
                    // but also generates a Block or Exit event in the case that either the process
                    // has more to process or is on its last CPU call respectively or a Timeout event
                    // in the matter that the duration of the CPU activity is larger than the time quantum
                    for (int x = 0; x < prioCount - 1; x++)
                    {
                        if (CPU == null && prioReady.get(x).size() != 0)
                        {
                            //This is the first come first serve selection out of the
                            // number of queues based on if there are elements in the queue
                            if (prioReady.get(x).size() != 0)
                            {
                                CPU = prioReady.get(x).get(0);
                                prioReady.get(x).remove(0);
                            }

                            CPU.getResponse().add(CurrentTime);

                            CPU.getEvent();
                            CPU.getEventTime();

                            if (CPU.getStart() == -1)
                                CPU.setStart(CurrentTime);

                            Event NewEvent = new Event();

                            if (CPU.getNumEvent() == 1 && CPU.getEventTime() <= TimeQuantum)
                            {
                                NewEvent.setEvent("EXIT");
                                NewEvent.setStamp(CurrentTime + CPU.getEventTime());
                            }
                            else if (CPU.getEventTime() > TimeQuantum)
                            {
                                NewEvent.setEvent("TIMEOUT");
                                NewEvent.setStamp(CurrentTime + TimeQuantum);
                            }
                            else if (CPU.getEventTime() <= TimeQuantum)
                            {
                                NewEvent.setEvent("BLOCK");
                                NewEvent.setStamp(CurrentTime + CPU.getEventTime());
                            }

                            NewEvent.setPID(CPU);

                            PriorityEvent.add(NewEvent);
                            PriorityEvent.sort(new eventComparator());

                            break;
                        }
                    }
                }

                //This is the time routine that takes care of when to increment the time
                // based on the event queue as well as incrementing the services times of
                // processes and decrementing the amount of time the process has left on the CPU
                if (CurrentTime < PriorityEvent.get(0).getStamp())
                {
                    delta = PriorityEvent.get(0).getStamp() - CurrentTime;

                    CurrentTime += delta;

                    //This is the service time section
                    if (CPU != null)
                    {
                        runningQuantum += delta;

                        CPU.setService(CPU.getService() + delta);
                        if (CPU.getEventTime() == delta)
                            CPU.setEventTime(-1);
                        else
                            CPU.setEventTime(CPU.getEventTime() - delta);
                    }

                    //This is the decrement time section for IO activities
                    if (Blocked.size() != 0)
                    {
                        for (process p : Blocked)
                        {
                            if (p.getEventTime() == delta)
                                p.setEventTime(-1);
                            else
                                p.setEventTime(p.getEventTime() - delta);
                        }
                    }
                }
            }
        }

        //This is used to sort the processes in the Exit queue based on their process ID's to later be printed in order
        Exit.sort(new processComparator());

        double turn = 0,
               turnN = 0,
               AResponseT = 0;

        //This is a for each loop for the processes in the Exit queue
        for (process p: Exit)
        {
            System.out.println("Process " + p.getID() + " :");
            System.out.println("\tArrive = " + p.getArrive());
            System.out.println("\tService = " + p.getService());
            System.out.println("\tStart = " + p.getStart());
            System.out.println("\tFinish = " + p.getFinish());
            System.out.println("\tTurnaround = " + p.getTurnaround());
            System.out.println("\tNormalized Turnaround = " + p.getNormTurnaround());
            System.out.println("\tAverage Response Time = " + p.getAverageResponseTime());

            turn += p.getTurnaround();
            turnN += p.getNormTurnaround();
            AResponseT += p.getAverageResponseTime();
        }

        //These are average stats across all the processes
        System.out.println("Mean Turnaround: " + (turn / Exit.size()));
        System.out.println("Mean Normalized Turnaround: " + (turnN / Exit.size()));
        System.out.println("Mean Average Response Time: " + (AResponseT / Exit.size()));
    }

    //This is the process exit routine function that is used to set values when a process enters the exit queue
    public static void Process_Exit_Routine(process Leaving, int CurrentTime)
    {
        int runningTime = 0;
        for (int x = 0; x < Leaving.getResponse().size(); x+=2)
        {
            runningTime += Leaving.getResponse().get(x + 1) - Leaving.getResponse().get(x);
        }

        Leaving.setFinish(CurrentTime);
        Leaving.setAverageResponseTime((double)runningTime / (double)(Leaving.getResponse().size()/2));
        Leaving.setTurnaround(Leaving.getFinish() - Leaving.getArrive());
        Leaving.setNormTurnaround(Leaving.getTurnaround() / (double)Leaving.getService());
    }
}

//This the Event classed used to take advantage and move this event based simulation
class Event implements Comparable<Event>
{
    private String event;
    private int stamp;
    private process P;

    public Event() {}

    public Event(String type, int time, process ID)
    {
        event = type;
        stamp = time;
        P = ID;
    }


    public String getEvent() {return event;}
    public int getStamp() {return stamp;}
    public process getP() {return P;}

    public void setEvent(String event) {this.event = event;}
    public void setStamp(int stamp) {this.stamp = stamp;}
    public void setPID(process PID) {this.P = PID;}

    @Override
    public int compareTo(Event object) {return this.stamp - object.getStamp();}
}

//This comparator was used to sort the events based on their time stamp just in case any events were entered out of order
class eventComparator implements Comparator<Event>
{
    public int compare(Event object1, Event object2) {return object1.getStamp() - object2.getStamp();}
}

//This is the process class used to hold certain statistics of each process and be used as the PCB of each process
class process implements Comparable<process>
{
    private int ID;
    private int Arrive;
    private int Service;
    private int Start;
    private int Finish;
    private int Turnaround;
    private double NormTurnaround;
    private double AverageResponseTime;
    private String Line;
    private int eventTime;
    private String Event;
    private ArrayList<Integer> Response = new ArrayList<>();
    private int NumEvent;
    private double SP;
    private int PrioNum;

    public process()
    {
        setService(0);
        setStart(-1);
        setSP(0);
        setPrioNum(0);
    }

    public int getID()                      {return ID;}
    public int getArrive()                  {return Arrive;}
    public int getService()                 {return Service;}
    public int getStart()                   {return Start;}
    public int getFinish()                  {return Finish;}
    public int getTurnaround()              {return Turnaround;}
    public double getNormTurnaround()       {return NormTurnaround;}
    public double getAverageResponseTime()  {return AverageResponseTime;}
    public String getLine()                 {return Line;}
    public ArrayList<Integer> getResponse() {return Response;}
    public double getSP()                   {return SP;}
    public int getPrioNum()                 {return PrioNum;}

    public void setID(int id)                                       {ID = id;}
    public void setArrive(int arrive)                               {Arrive = arrive;}
    public void setService(int service)                             {Service = service;}
    public void setStart(int start)                                 {Start = start;}
    public void setFinish(int finish)                               {Finish = finish;}
    public void setTurnaround(int turnaround)                       {Turnaround = turnaround;}
    public void setNormTurnaround(double normTurnaround)            {NormTurnaround = normTurnaround;}
    public void setAverageResponseTime(double averageResponseTime)  {AverageResponseTime = averageResponseTime;}
    public void setLine(String line)                                {Line = line;}
    public void setEventTime(int time)                              {eventTime = time;}
    public void setResponse(ArrayList<Integer> response)            {Response = response;}
    public void setPrioNum(int number)                              {PrioNum = number;}

    public String getEvent()
    {
        if (!Line.equals(""))
        {
            Event = Line.substring(0, Line.indexOf(' '));
        }
        return Event;
    }

    public int getEventTime()
    {
        if (eventTime == 0 && Line.chars().filter(ch -> ch == ' ').count() != 1)
        {
            if (!Line.equals(""))
            {
                String time = Line.substring(Line.indexOf(' ') + 1);
                time = time.substring(0, time.indexOf(' '));
                eventTime = Integer.parseInt(time);
            }
            return eventTime;
        }
        else if (eventTime == 0 && Line.chars().filter(ch -> ch == ' ').count() == 1)
        {
            if (!Line.equals(""))
            {
                String time = Line.substring(Line.indexOf(' ') + 1);
                eventTime = Integer.parseInt(time);
            }
            return eventTime;
        }
        return eventTime;
    }

    public void nextEvent()
    {
        if (Line.chars().filter(ch -> ch == ' ').count() == 1)
            Line = "";
        else
        {
            String temp = Line.substring(Line.indexOf(' ') + 1);
            temp = temp.substring(temp.indexOf(' ') + 1);

            Line = temp;
        }
    }

    //This function is used to get the number of events that are currently in the process
    public int getNumEvent()
    {
        if ((int)Line.chars().filter(ch -> ch == ' ').count() != 1)
            return ((int)Line.chars().filter(ch -> ch == ' ').count()+1) / 2;
        return (int)Line.chars().filter(ch -> ch == ' ').count();
    }

    //This function is used to generate the SP value for exponential averaging
    public void setSP(int nextNum)
    {
        double alpha = 0.8;

        if (SP == 0)
            SP = nextNum;
        else
            SP = alpha * (nextNum) + (1 - alpha) * SP;
    }

    @Override
    public int compareTo(process object) {return this.ID - object.getID();}
}

//This class was created so the dot sort function would be available for my process class to be organized by their process ID's
class processComparator implements Comparator<process>
{
    public int compare(process object1, process object2) {return object1.getID() - object2.getID();}
}
