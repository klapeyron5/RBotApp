package space.klapeyron.rbotapp;

import android.util.Log;

import java.util.ArrayList;

import ru.rbot.android.bridge.service.robotcontroll.controllers.BodyController;
import ru.rbot.android.bridge.service.robotcontroll.controllers.body.TwoWheelsBodyController;
import ru.rbot.android.bridge.service.robotcontroll.exceptions.ControllerException;
import ru.rbot.android.bridge.service.robotcontroll.robots.Robot;

public class TaskHandler {

    MainActivity mainActivity;
    float startPath;
    float startAngle;
    Robot robot;

    private final static float forwardDistance = 0.5f;


    int[] arrayPath = {1,1,2,1};
    ArrayList<Integer> path;//0-right; 1-forward;2-left;

    TaskHandler(MainActivity m) {
        mainActivity = m;
        robot = mainActivity.robot;
    }

    public void setTask() throws ControllerException {
    //    Navigation navigation = new Navigation();
    //    path = navigation.getPath();

        path = new ArrayList<>();
        arrayInList();//TODO
        TaskThread taskThread = new TaskThread();
        taskThread.start();

    }

    class TaskThread extends Thread {
        @Override
        public void run() {
            float startPath = mainActivity.passedWay;
            Log.i(MainActivity.TAG, "setTask start passedWay " + startPath);

            int straightLineCoeff = 0;
            //     distanceForward(straightLineCoeff);
            for(int i=0;i<path.size();i++) {
                    switch(path.get(i)) {
                        case 0:
                            break;
                        case 1:
                            //     if (path.get(i - 1) == 1)
                            straightLineCoeff++;
                            if (i == path.size() - 1) {
                                distanceForward(straightLineCoeff);
                                straightLineCoeff = 0;
                            } else
                            if (path.get(i + 1) != 1) {
                                distanceForward(straightLineCoeff);
                                straightLineCoeff = 0;
                            }
                            break;
                        case 2:
                            turnLeft();
                            //  left();
                            //  sleep(2500);
                            break;
                    }
            }
            Log.i(MainActivity.TAG, "setTask finish passedWay " + mainActivity.passedWay);
            Log.i(MainActivity.TAG, "setTask finish difference " + (mainActivity.passedWay-startPath));
        }
    }

    private void distanceForward(int straightLineCoeff) {
        //     Log.i(MainActivity.TAG, "forwardThread started "+straightLineCoeff);
    /*    StartingForwardThread startingForwardThread = new StartingForwardThread();
        startingForwardThread.start(); //acceleration on first forwardDistance
        try {
            startingForwardThread.join();
            Log.i(MainActivity.TAG, "startingForwardThread join()");
        } catch (InterruptedException e) {}
        straightLineCoeff--;*/

        if(straightLineCoeff > 0) {
            ForwardThread forwardThread = new ForwardThread(straightLineCoeff);
            forwardThread.start();
            try {
                forwardThread.join();
                Log.i(MainActivity.TAG, "forwardThread join()");
            } catch (InterruptedException e) {}
        }
    }

    private void turnLeft() {
        LeftThread leftThread = new LeftThread();
        leftThread.start();
        try {
            leftThread.join();
            Log.i(MainActivity.TAG, "leftThread join()");
        } catch (InterruptedException e) {}
    }

    class StartingForwardThread extends Thread {
        private float startPath;
        private float[] accelerationSpeeds = {};

        StartingForwardThread() {
            startPath = mainActivity.passedWay;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "StartingForwardThread started ");
            if( robot.isControllerAvailable( BodyController.class ) )
            {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController( BodyController.class );
                    if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                    {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                        float i = 0;
                        while(true) {
                            if(i<20) //acceleration
                                i++;
                            if(mainActivity.passedWay - startPath < TaskHandler.forwardDistance)
                                try {
                                    wheelsController.setWheelsSpeeds(i,i);
                                    sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            else {
                                //        lowLevelNavigationMethods.stopWheelsAction(lowLevelNavigationKey);
                                Log.i(MainActivity.TAG, "StartingForwardThread finished " + Float.toString(mainActivity.passedWay - startPath));
                                return;
                            }
                        }
                    }
                } catch (ControllerException e) {}
            }
        }
    }

    class ForwardThread extends Thread {
        private float startPath;
        private float purposePath;

        ForwardThread(int straightLineCoeff) {
            startPath = mainActivity.passedWay;
            purposePath = straightLineCoeff * TaskHandler.forwardDistance;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "ForwardThread started");
            if( robot.isControllerAvailable( BodyController.class ) )
            {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController( BodyController.class );
                    if( bodyController.isControllerAvailable( TwoWheelsBodyController.class ) )
                    {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController( TwoWheelsBodyController.class );
                        wheelsController.moveForward(20f,100f);
                        while(true) {
                                if (mainActivity.passedWay - startPath < purposePath) {
                                } else {
                                    wheelsController.setWheelsSpeeds(0.0f, 0.0f);
                                    try {
                                        sleep(200);
                                    } catch (InterruptedException e) {}
                                    Log.i(MainActivity.TAG, "ForwardThread finished " + Float.toString(mainActivity.passedWay - startPath));
                                    return;
                                }
                        }
                    }
                } catch (ControllerException e) {}
            }
        }
    }

    class LeftThread extends Thread {
        private float startAngle;
        private float purposeAngle;

        LeftThread() {
            startAngle = mainActivity.angle;
            purposeAngle = (float) Math.PI / 2; //TODO //average correcting
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "LeftThread started ----->>>>>>" + startAngle);

            int flagVariant;
            if ((startAngle >= 0) && (startAngle < Math.PI / 2)) {
                flagVariant = 1;
                Log.i(MainActivity.TAG, "1");
            } else if (startAngle > Math.PI / 2) {
                flagVariant = 2;
                Log.i(MainActivity.TAG, "2");
            } else if (startAngle < -Math.PI / 2) {
                flagVariant = 3;
                Log.i(MainActivity.TAG, "3");
            } else {
                flagVariant = 4;
                Log.i(MainActivity.TAG, "4");
            }

            if (robot.isControllerAvailable(BodyController.class)) {
                BodyController bodyController = null;
                try {
                    bodyController = (BodyController) robot.getController(BodyController.class);
                    if (bodyController.isControllerAvailable(TwoWheelsBodyController.class)) {
                        TwoWheelsBodyController wheelsController = null;
                        wheelsController = (TwoWheelsBodyController) bodyController.getController(TwoWheelsBodyController.class);
                        wheelsController.turnAround(10f,(float)Math.PI/2);
                        while (true) {
                                if (new FlagVariant(flagVariant).getFlag()) {
                                } else {
                                    wheelsController.setWheelsSpeeds(0.0f, 0.0f);
                                    try {
                                        sleep(200);
                                    } catch (InterruptedException e) {}
                                    Log.i(MainActivity.TAG, "LeftThread finished ------------>>>>> " + new FlagVariant(flagVariant).getDimension());
                                    return;
                                }
                        }
                    }
                } catch (ControllerException e) {
                }
            }
        }

        class FlagVariant {
            private int variant = 0;

            FlagVariant(int v) {
                variant = v;
            }

            public boolean getFlag() {
                float currentAngle = mainActivity.angle;
                switch (this.variant) {
                    case 1:
                        return ((currentAngle - startAngle) < purposeAngle);
                    case 2:
                        if (currentAngle < 0)
                            currentAngle += 2 * Math.PI;
                        return ((currentAngle - startAngle) < purposeAngle);
                    case 3:
                        return ((currentAngle - startAngle) < purposeAngle);
                    case 4:
                        return ((currentAngle - startAngle) < purposeAngle);
                    default:
                        return true;
                }
            }

            public float getDimension() {
                float currentAngle = mainActivity.angle;
                switch (this.variant) {
                    case 1:
                        return (currentAngle - startAngle);
                    case 2:
                        if (currentAngle < 0)
                            currentAngle += 2 * Math.PI;
                        return (currentAngle - startAngle);
                    case 3:
                        return (currentAngle - startAngle);
                    case 4:
                        return (currentAngle - startAngle);
                    default:
                        return 0;
                }
            }
        }
    }

    //TODO
    private void arrayInList() {
        for(int i=0;i<arrayPath.length;i++)
            path.add(arrayPath[i]);
    }
}