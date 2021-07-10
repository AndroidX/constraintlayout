package androidx.constraintLayout.desktop.link;

import androidx.constraintlayout.core.parser.CLParser;
import androidx.constraintlayout.core.parser.CLParsingException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;


public class MotionLink {
    private static final int UPDATE_CONTENT = 1;
    private static final int UPDATE_PROGRESS = 2;
    private static final int GET_CURRENT_CONTENT = 3;
    private static final int SET_DRAW_DEBUG = 4;
    private static final int GET_LAYOUT_LIST = 5;
    private static final int GET_CURRENT_LAYOUT = 6;
    private static final int UPDATE_LAYOUT_DIMENSIONS = 7;

    private boolean dispatchOnUIThread = true;

    DataOutputStream writer;
    DataInputStream reader;
    Socket socket;

    public String errorMessage;
    public String statusMessage;
    public int mSelectedIndex;

    public enum Event {
        STATUS,  // general status messages
        ERROR,    // error status messages
        LAYOUT_LIST_UPDATE,  // layout List updated
        MOTION_SCENE_UPDATE, // main text update
        LAYOUT_UPDATE,
    }

    private boolean connected = false;

    public String[] layoutNames;
    public String selectedLayoutName = "test2";
    public String motionSceneText; // Big MotionScene String
    public String layoutInfos;

   public interface DataUpdateListener {
        void update(Event event, MotionLink link);
    }

    ArrayList<DataUpdateListener> listeners = new ArrayList<>();

    Vector<Runnable> mTaskQue = new Vector<>();
    Thread taskThread = new Thread(() -> executeTask());

    void executeTask() {
        synchronized (mTaskQue) {
            try {
                for (; ; ) {
                    while (mTaskQue.isEmpty()) {
                        mTaskQue.wait();
                    }
                    Runnable task = mTaskQue.remove(0);
                    task.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void addTask(Runnable r) {
        synchronized (mTaskQue) {
            mTaskQue.add(r);
            mTaskQue.notify();
        }
    }

    public MotionLink() {
        taskThread.setDaemon(true);
        taskThread.setName("MotionLink");
        taskThread.start();
    }

    public void addListener(DataUpdateListener listener) {
        listeners.add(listener);
    }

    private void prepareConnection() {
        if (!connected || !socket.isConnected()) {
            reconnect();
        }
    }

    private void reconnect() {
        try {
            if (connected) {
                socket.close();
            }
            socket = new Socket("localhost", 9999);
            writer = new DataOutputStream(socket.getOutputStream());
            reader = new DataInputStream(socket.getInputStream());
            connected = true;
        } catch (Exception e) {
            loge("Could not connect to application " + e.getMessage());
        }
    }

    public void getLayoutList() {
        addTask(this::_getLayoutList);
    }

    private void _getLayoutList() {
        try {
            prepareConnection();
            writer.writeInt(GET_LAYOUT_LIST);
            writer.writeUTF(selectedLayoutName);
            var numLayouts = reader.readInt();

            log("found layouts " + numLayouts);
            layoutNames = new String[numLayouts];
            for (int i = 0; i < numLayouts; i++) {
                layoutNames[i] = reader.readUTF();
            }
            notifyListeners(Event.LAYOUT_LIST_UPDATE);

        } catch (Exception e) {
            reconnect();
        }
    }

    public void sendProgress(float value) {
        addTask(() -> _sendProgress(value));
        updateLayoutInformation();
    }

    private void _sendProgress(Float value) {
        try {
            prepareConnection();
            writer.writeInt(UPDATE_PROGRESS);
            writer.writeUTF(selectedLayoutName);
            writer.writeFloat(value);
        } catch (Exception e) {
            reconnect();
        }
    }

    public void sendLayoutDimensions(int width, int height) {
        addTask(() -> _sendLayoutDimensions(width, height));
        updateLayoutInformation();
    }

    private void _sendLayoutDimensions(int width, int height) {
        try {
            prepareConnection();
            writer.writeInt(UPDATE_LAYOUT_DIMENSIONS);
            writer.writeUTF(selectedLayoutName);
            writer.writeInt(width);
            writer.writeInt(height);
        } catch (Exception e) {
            reconnect();
        }
    }

    public void getContent() {
        addTask(this::_getContent);
    }

    public void _getContent() {
        try {
            prepareConnection();
            writer.writeInt(GET_CURRENT_CONTENT);
            writer.writeUTF(selectedLayoutName);
            motionSceneText = reader.readUTF();
            notifyListeners(Event.MOTION_SCENE_UPDATE);
        } catch (Exception e) {
            reconnect();
        }
    }

    public void selectMotionScene(int index) {
        mSelectedIndex = index;
        selectedLayoutName = layoutNames[index];
    }

    public void selectMotionScene(String name) {
        for (int i = 0; i < layoutNames.length; i++) {
           if (layoutNames[i].equals(name)) {
                mSelectedIndex = i;
                selectedLayoutName = name;
            }
        }
    }

    Timer timer = null;

    public void setUpdateLayoutPolling(boolean run) {
        if (run && timer == null) {
            timer = new Timer(15, e -> {
                updateLayoutInformation();
            });
            timer.setRepeats(true);
            timer.start();
        } else if (!run) {
            if (timer != null) {
                timer.stop();
            }
            timer = null;
        }
    }

    public void updateLayoutInformation() {
        addTask(this::_updateLayoutInformation);
    }

    private void _updateLayoutInformation() {
        try {
            prepareConnection();
            writer.writeInt(GET_CURRENT_LAYOUT);
            writer.writeUTF(selectedLayoutName);
            layoutInfos = reader.readUTF();
            notifyListeners(Event.LAYOUT_UPDATE);
        } catch (Exception e) {
            loge("Could not connect to application " + e.getMessage());
            reconnect();
        }
    }

    public void sendContent(String value) {
        try {
            CLParser.parse(value);
            addTask(() -> _sendContent(value));
            updateLayoutInformation();
        } catch (CLParsingException e) {

        }
    }

    public void _sendContent(String content) {
        try {
            prepareConnection();
            writer.writeInt(UPDATE_CONTENT);
            writer.writeUTF(selectedLayoutName);
            writer.writeUTF(content);
        } catch (Exception e) {
            loge("connection issue " + e.getMessage());
            reconnect();
        }
    }

    private void notifyListeners(Event event) {
        if (dispatchOnUIThread) {
            SwingUtilities.invokeLater(() -> {
                for (DataUpdateListener listener : listeners) {
                    listener.update(event, this);
                }
            });
        } else {
            for (DataUpdateListener listener : listeners) {
                listener.update(event, this);
            }
        }
    }

    public void setDrawDebug(Boolean active) {
        addTask(() -> _setDrawDebug(active));
    }

    public void _setDrawDebug(Boolean active) {
        try {
            prepareConnection();
            writer.writeInt(SET_DRAW_DEBUG);
            writer.writeUTF(selectedLayoutName);
            writer.writeBoolean(active);
        } catch (Exception e) {
            loge("connection issue " + e.getMessage());
            reconnect();
        }
    }

    private void loge(String err) {
        errorMessage = err;
        notifyListeners(Event.ERROR);
        System.err.println(err);
    }

    private void log(String err) {
        System.out.println(err);
    }

    public static void main(String[] arg) throws InterruptedException {
        MotionLink motionLink = new MotionLink();
        long start = System.nanoTime();
        motionLink.addListener(((event, link) -> {
            long time = System.nanoTime() - start;
            System.out.println(((int) (time * 1E-6)) + ": " + event);
            switch (event) {
                case ERROR:
                    System.out.println(link.errorMessage);
                    link.errorMessage = "";
                    break;
                case LAYOUT_UPDATE:
                    System.out.println(link.layoutInfos);
                    break;
                case LAYOUT_LIST_UPDATE:
                    System.out.println(Arrays.toString(link.layoutNames));
                    break;
                case MOTION_SCENE_UPDATE:
                    System.out.println(link.motionSceneText);
                    break;

            }
        }));

        motionLink.getLayoutList();
        Thread.sleep(1000);
        motionLink.selectMotionScene(0);
        motionLink.getContent();
        Thread.sleep(10000);


    }
}
