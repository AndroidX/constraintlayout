/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.validation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;


import android.support.constraintlayout.validation.R;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.constraintlayout.core.Metrics;
import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.constraintlayout.core.Metrics;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends AppCompatActivity implements Server.Requests {

    private static final boolean REVERSE = false;
    private static final boolean USE_ZIP = true;
    private static final int ITERATIONS = 2;
    private final boolean DEBUG = false;

    private String SHOW_FIRST = "check*";

    Server mServer = new Server(4242);
    HashMap<String, Command> mCommands = new HashMap<>();
    HashMap<String, TestLayout> mTests = new HashMap<>();
    HashMap<String, Integer> mTestsDelay = new HashMap<>();
    HashSet<String> mSkippedTests = new HashSet<>();

    TestLayout testLayout_252 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        int left = ((ViewGroup) view.getParent()).getPaddingLeft() + layoutParams.leftMargin;
        int top = ((ViewGroup) view.getParent()).getPaddingTop() + layoutParams.topMargin;
        view.layout(left, top, view.getMeasuredWidth(), view.getMeasuredHeight());
        TextInputEditText editText = view.findViewById(R.id.textinput);
        editText.setText("ABC SDPOfpsdofjkpso dsd spdokfpsd sp oskdfpoksd fsd pokspdkf " +
                "sdpf dspofkpdsokf sd poskpdsokf sdf pokspdofk dsf psokdspfo kdspofk dsf");
        view.measure(widthMeasureSpec, heightMeasureSpec);
        editText.setText("ABC");
        return false;
    };

    TestLayout testLayout_253 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        int left = ((ViewGroup) view.getParent()).getPaddingLeft() + layoutParams.leftMargin;
        int top = ((ViewGroup) view.getParent()).getPaddingTop() + layoutParams.topMargin;
        view.layout(left, top, view.getMeasuredWidth(), view.getMeasuredHeight());
        LinearLayout linearLayout = view.findViewById(R.id.view0);
        TextView textView = new TextView(view.getContext());
        textView.setText("Lorem ipsum dolor sit amet");
        textView.setTextSize(40f);
        linearLayout.addView(textView);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return false;
    };

    TestLayout testLayout_265 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        int left = ((ViewGroup) view.getParent()).getPaddingLeft() + layoutParams.leftMargin;
        int top = ((ViewGroup) view.getParent()).getPaddingTop() + layoutParams.topMargin;
        view.layout(left, top, view.getMeasuredWidth(), view.getMeasuredHeight());
        TextInputLayout textInputLayout = view.findViewById(R.id.til_phone_number_1);
        TextInputEditText editText = view.findViewById(R.id.et_phone_number_1);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        ViewTreeObserver observer = view.getViewTreeObserver();
        view.post(() -> {
            InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            view.postDelayed(() -> textInputLayout.setError("some error"), 1000);
            view.postDelayed(() -> {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }, 1600);
        });
        return true;
    };

    TestLayout testLayout_335 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        View view1 = view.findViewById(R.id.button1);
        View view2 = view.findViewById(R.id.button2);

        view.postDelayed(() -> {
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
        }, 500);
        return true;
    };

    TestLayout testLayout_339 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        RecyclerView list_view = view.findViewById(R.id.list_view);
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("Test String 1");
        xVals.add("Test String 2");
        xVals.add("Test String 3");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list_view.setLayoutManager(linearLayoutManager);
        list_view.setAdapter(new Adapter339(view.getContext(), xVals));
        return false;
    };

    TestLayout testLayout_376 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        ConstraintLayout root = findViewById(R.id.root);
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        FrameLayout frameLayout = findViewById(R.id.googleapp_incognito_topbar_container);
        ViewCompat.setOnApplyWindowInsetsListener(
                frameLayout,
                (v, insets) -> {
                    int systemWindowInsetTop = insets.getSystemWindowInsetTop();

                    // Adjust the height of the topBar by extending it from status bar.
                    int topbarHeight = this.getApplicationContext()
                            .getResources()
                            .getDimensionPixelSize(R.dimen.incognito_topbar_height);
                    ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                    params.height = topbarHeight + systemWindowInsetTop;
                    frameLayout.setLayoutParams(params);
                    return insets;
                });
        ViewCompat.requestApplyInsets(frameLayout);
//        frameLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
//            @Override
//            public void onViewAttachedToWindow(View v) {
//                v.removeOnAttachStateChangeListener(this);
//                v.requestApplyInsets();
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//
//            }
//        });
        return false;
    };

    TestLayout testLayout_397 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        Fragment fragment = new CLBugFragment();
        view.postDelayed(() -> {
            getSupportFragmentManager().beginTransaction().add(R.id.host, fragment).commit();
        }, 20);
        return true;
    };

    TestLayout testLayout_398 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        View button = view.findViewById(R.id.button2);
        ConstraintLayout layout = (ConstraintLayout) view;
        layout.removeView(button);
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return true;
    };

    TestLayout testLayout_406 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        //Layer layer = view.findViewById(R.id.layer);
        //layer.setTag("TOTO");
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        //System.out.println("layer.getTag: " + layer.getTag());
        //if (!layer.getTag().equals("TOTO")) {
        //    return false;
        //}
        return true;
    };

    TestLayout testLayout_408 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        View viewToRemove = findViewById(R.id.issue);
        ViewGroup group = (ViewGroup) viewToRemove.getParent();
        viewToRemove.postDelayed(() -> {
            group.removeView(viewToRemove);
        }, 20);
        return true;
    };

    TestLayout testLayout_420 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        view.forceLayout();
        view.measure(widthMeasureSpec, heightMeasureSpec);
        View viewToResize = findViewById(R.id.issue);
        viewToResize.postDelayed(() -> {
            FrameLayout.LayoutParams layoutParamsView = (FrameLayout.LayoutParams) viewToResize.getLayoutParams();
            layoutParamsView.height = 400;
            viewToResize.setLayoutParams(layoutParamsView);
        }, 20);
        return true;
    };

    TestLayout testLayout_428 = (view, mode, widthMeasureSpec, heightMeasureSpec, layoutParams, rootView) -> {
        MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select Appointment time")
                .build();
        picker.show(getSupportFragmentManager(), "time");
        if (rootView != null) {
            rootView[0] = picker;//.getDialog().getWindow().getDecorView();
        }
        return true;
    };

    Command listCommand = (activity, server, writer, reader, out) -> {
        String[] layouts = getLayouts();
        for (int i = 0; i < layouts.length; i++) {
            writer.println(layouts[i]);
        }
    };

    Command loadAndMeasure = (activity, server, writer, reader, out) -> {
        String layoutName = reader.nextLine();
        if (DEBUG) {
            System.out.println("we need to load " + layoutName);
        }
        String mode = reader.nextLine();
        if (DEBUG) {
            System.out.println("we need to measure " + mode);
        }
        int readOptimization = Integer.parseInt(reader.nextLine());
        int optimization = readOptimization;
        if (DEBUG) {
            System.out.println("we need to use optimization " + optimization);
        }

        String[] result = new String[1];
        int[] measureSpecs = new int[2];
        Fragment[] fragment = new Fragment[1];

        int delay = 0;
        if (mTestsDelay.containsKey(layoutName)) {
            delay = mTestsDelay.get(layoutName);
        }
        if (delay > 0) {
            int timeout = delay;
            server.runOnUiAndWait(activity, () -> {
                View view = setupLayout(activity, layoutName, mode, measureSpecs, optimization);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                if (mTests.containsKey(layoutName)) {
                    TestLayout testLayout = mTests.get(layoutName);
                    if (testLayout.apply(view, mode, measureSpecs[0], measureSpecs[1], layoutParams, fragment)) {
                        return;
                    }
                }
                result[0] = measureLayout(view, layoutParams, measureSpecs[0], measureSpecs[1]);
            });
            if (result[0] == null) {
                try {
                    Thread.sleep(timeout);
                    server.runOnUiAndWait(activity, () -> {
                        ViewGroup host = activity.findViewById(android.R.id.content);
                        if (fragment[0] != null && fragment[0] instanceof DialogFragment) {
                            DialogFragment dialogFragment = (DialogFragment) fragment[0];
                            View decorView = dialogFragment.getDialog().getWindow().getDecorView();
                            if (decorView != null && decorView instanceof ViewGroup) {
                                host = (ViewGroup) decorView;
                            }
                        }
                        View view = host.getChildAt(0);
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        result[0] = measureLayout(view, layoutParams, measureSpecs[0], measureSpecs[1]);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            server.runOnUiAndWait(activity, () -> {
                View view = setupLayout(activity, layoutName, mode, measureSpecs, optimization);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                boolean validTest = true;
                if (mTests.containsKey(layoutName)) {
                    TestLayout testLayout = mTests.get(layoutName);
                    validTest = testLayout.apply(view, mode, measureSpecs[0], measureSpecs[1], layoutParams, fragment);
                    validTest = true;
                }
                if (validTest) {
                    result[0] = measureLayout(view, layoutParams, measureSpecs[0], measureSpecs[1]);
                } else {
                    String res = "{";
                    res += pair("duration", "" + -1) + ", ";
                    res += "error : \"test\", ";
                    res += "layout : " + serialize(view);
                    res += "}";
                    result[0] = res;
                }
            });
        }
        writer.println(result[0]);
    };

    private void setOptimizations(ViewGroup root, int optimizations) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View view = root.getChildAt(i);
            if (view instanceof ViewGroup) {
                setOptimizations((ViewGroup) view, optimizations);
            }
            if (view instanceof ConstraintLayout) {
                ConstraintLayout constraintLayout = (ConstraintLayout) view;
                constraintLayout.setOptimizationLevel(optimizations);
                constraintLayout.forceLayout();
            }
        }
    }

    @NonNull
    private View setupLayout(Activity activity, String layoutName, String mode, int[] measureSpecs, int optimization) {
        loadLayout(layoutName);
        ViewGroup host = activity.findViewById(android.R.id.content);
        View view = host.getChildAt(0);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        setOptimizations(host, optimization);
        //System.out.println("setup layout " + layoutName + " mode " + mode + " opt " + optimization);
        int parentWidth = host.getWidth();
        int parentHeight = host.getHeight();
        int widthMeasureSpec = 0;
        int heightMeasureSpec = 0;
        if (mode.equals("MATCH_MATCH")) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.EXACTLY);
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (mode.equals("MATCH_WRAP")) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.AT_MOST);
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if (mode.equals("WRAP_MATCH")) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.AT_MOST);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.EXACTLY);
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (mode.equals("WRAP_WRAP")) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.AT_MOST);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.AT_MOST);
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        measureSpecs[0] = widthMeasureSpec;
        measureSpecs[1] = heightMeasureSpec;

        view.setLayoutParams(layoutParams);
        return view;
    }

    long totalMeasuredWidgets = 0;
    long totalMeasuredMatchWidgets = 0;
    long totalGrouping = 0;
    long totalLayouts = 0;

    private String measureLayout(View view, FrameLayout.LayoutParams layoutParams, int widthMeasureSpec, int heightMeasureSpec) {
        long start = System.nanoTime();
        int iterations = ITERATIONS;
//        androidx.constraintlayout.core.Metrics metrics = new Metrics();
        if (view instanceof ConstraintLayout) {
//            ((ConstraintLayout) view).fillMetrics(metrics);
            //metrics.measuresLayoutDuration = 0;
//            metrics.grouping = 0;
//            metrics.layouts = 0;
        }
        for (int i = 0; i < iterations; i++) {
            view.forceLayout();
            view.measure(widthMeasureSpec, heightMeasureSpec);
            int left = ((ViewGroup) view.getParent()).getPaddingLeft() + layoutParams.leftMargin;
            int top = ((ViewGroup) view.getParent()).getPaddingTop() + layoutParams.topMargin;
            view.layout(left, top, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        long duration = (System.nanoTime() - start) / iterations;

//        totalGrouping += metrics.grouping;
//        totalLayouts += metrics.layouts;

//        long measuresDuration = metrics.measuresWidgetsDuration / iterations;
//        long layoutDuration = metrics.measuresLayoutDuration / iterations;
//        //System.out.println("total duration = " + duration + " layout " + layoutDuration + " measures " + measuresDuration);
//        //duration = layoutDuration;// - measuresDuration;
//        totalMeasuredWidgets += (metrics.measuredWidgets / iterations);
//        totalMeasuredMatchWidgets += (metrics.measuredMatchWidgets / iterations);
//        System.out.println("Total Measures: " + totalMeasuredWidgets
        //              + " matched " + totalMeasuredMatchWidgets);

        //        duration = measureDuration / iterations;
        //ArrayLinkedVariables.metrics();
        if (duration == 0) {
//            duration = 2000;
        }
//        System.out.println("total duration = " + duration + " layout " + layoutDuration + " measures " + measuresDuration);
//        System.out.println("total: " +  totalLayouts + " grouping " + totalGrouping);
        return serializeRoot(view, duration);
    }

    Command loadFile = (activity, server, writer, reader, out) -> {
        String layoutName = reader.nextLine();
        if (DEBUG) {
            System.out.println("we need to load " + layoutName);
        }
        activity.runOnUiThread(() -> {
            loadLayout(layoutName);
        });
    };

    Command getLayout = (activity, server, writer, reader, out) -> {
        String[] result = new String[1];
        server.runOnUiAndWait(activity, () -> {
            if (DEBUG) {
                System.out.println("get layout on ui thread");
            }
            ViewGroup host = activity.findViewById(android.R.id.content);
            View view = host.getChildAt(0);
            result[0] = serializeRoot(view, 0);
            server.notifyUIDone();
        });
        writer.println(result[0]);
    };

    Command setMxM = (activity, server, writer, reader, out) -> {
        server.runOnUiAndWait(activity, () -> {
            ViewGroup host = activity.findViewById(android.R.id.content);
            View view = host.getChildAt(0);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);
        });
    };

    Command setMxW = (activity, server, writer, reader, out) -> {
        server.runOnUiAndWait(activity, () -> {
            ViewGroup host = activity.findViewById(android.R.id.content);
            View view = host.getChildAt(0);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(layoutParams);
        });
    };

    Command setWxM = (activity, server, writer, reader, out) -> {
        server.runOnUiAndWait(activity, () -> {
            ViewGroup host = activity.findViewById(android.R.id.content);
            View view = host.getChildAt(0);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);
        });
    };

    Command setWxW = (activity, server, writer, reader, out) -> {
        server.runOnUiAndWait(activity, () -> {
            ViewGroup host = activity.findViewById(android.R.id.content);
            View view = host.getChildAt(0);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(layoutParams);
        });
    };

    Command takePicture = (activity, server, writer, reader, out) -> {
        byte[][] byteArray = new byte[1][];
        int[] dimension = new int[2];
        server.runOnUiAndWait(activity, () -> {
            ViewGroup host = activity.findViewById(android.R.id.content);
            View view = host;//.getChildAt(0);
            int shrinkFactor = 1;
            int w = view.getWidth() / shrinkFactor;
            int h = view.getHeight() / shrinkFactor;
            if (w > 0 && h > 0) {
                dimension[0] = w;
                dimension[1] = h;
                Bitmap bitmap = Bitmap.createBitmap(dimension[0], dimension[1], Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                float scale = 1f / shrinkFactor;
                canvas.scale(scale, scale);
                view.draw(canvas);

                int size = bitmap.getRowBytes() * bitmap.getHeight();
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                bitmap.copyPixelsToBuffer(byteBuffer);
                byteArray[0] = byteBuffer.array();
            }
        });
        writer.println(Integer.toString(dimension[0]));
        writer.println(Integer.toString(dimension[1]));
        byte[] array = byteArray[0];
        if (array != null && array.length > 0) {
            if (USE_ZIP) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                try {
                    ZipEntry entry = new ZipEntry("image");
                    zipOutputStream.putNextEntry(entry);
                    zipOutputStream.write(array, 0, array.length);
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                    array = outputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.println(Integer.toString(array.length));
            writer.write(array);
        } else {
            writer.println(Integer.toString(0));
        }
    };

    private String pair(String key, String value) {
        return "\"" + key + "\" : \"" + value + "\"";
    }

    private String stripId(String id) {
        int index = id.indexOf("/");
        if (index > 0) {
            return id.substring(index + 1);
        }
        return id;
    }

    private String serializeRoot(View view, long duration) {
        String result = "{";
        result += pair("duration", "" + duration) + ", ";
        result += "layout : " + serialize(view);
        result += "}";
        //System.out.println("serialize : " + result);
        return result;
    }

    @SuppressLint("ResourceType")
    private String serialize(View view) {
        String result = "{";
        String name = view.getClass().getSimpleName();
        result += pair("class", name) + ", ";
        if (view.getId() != View.NO_ID && view.getId() > 1) {
            String viewId;
            try {
                viewId = view.getResources().getResourceName(view.getId());
            } catch (Resources.NotFoundException e) {
                viewId = "NOT_FOUND";
            }
            viewId = stripId(viewId);
            result += pair("id", viewId) + ", ";
        }
        String bounds = "{ " + pair("left", "" + view.getLeft())
                + ", " + pair("top", "" + view.getTop())
                + ", " + pair("right", "" + view.getRight())
                + ", " + pair("bottom", "" + view.getBottom()) + " }";
        result += "\"bounds\" : " + bounds;
        if (view instanceof ViewGroup) {
            result += ", \"children\" : [ ";
            ViewGroup viewGroup = (ViewGroup) view;
            final int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                result += serialize(viewGroup.getChildAt(i));
                if (i < count -1) {
                    result += ", ";
                }
            }
            result += " ] ";
        }
        result += "}";
        return result;
    }

    private void loadLayout(String name) {
        String layoutName = "R.layout." + name;
        int resource = getResources().getIdentifier(name, "layout", getPackageName());
        if (DEBUG) {
            System.out.println("load layout <" + layoutName + "> => " + resource);
        }
        if (resource != 0) {
            setContentView(resource);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommands.put("LIST", listCommand);
        mCommands.put("LOAD", loadFile);
        mCommands.put("GET_LAYOUT", getLayout);
        mCommands.put("MxM", setMxM);
        mCommands.put("MxW", setMxW);
        mCommands.put("WxM", setWxM);
        mCommands.put("WxW", setWxW);
        mCommands.put("LOAD_MEASURE", loadAndMeasure);
        mCommands.put("TAKE_PICTURE", takePicture);

        mTests.put("check_252", testLayout_252);
        mTests.put("check_253", testLayout_253);
        mTests.put("check_265", testLayout_265);
        mTests.put("check_335", testLayout_335);
        mTests.put("check_339", testLayout_339);
        mTests.put("check_376", testLayout_376);
        mTests.put("check_397", testLayout_397);
        mTests.put("check_398", testLayout_398);
        mTests.put("check_406", testLayout_406);
        mTests.put("check_408", testLayout_408);
        mTests.put("check_420", testLayout_420);
        mTests.put("check_428", testLayout_428);

        mTestsDelay.put("check_265", 1500);
        mTestsDelay.put("check_335", 1500);
        mTestsDelay.put("check_397", 1500);
        mTestsDelay.put("check_420", 500);
        mTestsDelay.put("check_428", 500);

        // We skip those for now
        // (depends on moving to LayoutParams instead of MarginLayoutParams)
        mSkippedTests.add("check_399");
        mSkippedTests.add("check_413");
        mSkippedTests.add("check_414");
        mSkippedTests.add("check_415");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Window window = getWindow();
            window.setSustainedPerformanceMode(true);
        }

//        setContentView(R.layout.activity_main);
//        setContentView(R.layout.check_339);
////
//        RecyclerView list_view = findViewById(R.id.list_view);
//        ArrayList<String> xVals = new ArrayList<String>();
//        xVals.add("Test String 1");
//        xVals.add("Test String 2");
//        xVals.add("Test String 3");
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        list_view.setLayoutManager(linearLayoutManager);
//        list_view.setAdapter(new Adapter339(getApplicationContext(), xVals));

        setContentView(R.layout.check_410);

        //setContentView(R.layout.check_251);
//        setContentView(R.layout.check_024);
//        setContentView(R.layout.check_003);
        mServer.setRequestsHandler(this);
        mServer.start();
    }

    private String[] getLayouts() {
        ArrayList<String> list = new ArrayList<>();
        Field[] f = R.layout.class.getDeclaredFields();

        Arrays.sort(f, (f1, f2) -> {
            int v = (REVERSE ? -1 : 1) * f1.getName().compareTo(f2.getName());
            if (SHOW_FIRST == null) {
                return v;
            }
            if (f1.getName().matches(SHOW_FIRST)) {
                return -1;
            }
            if (f2.getName().matches(SHOW_FIRST)) {
                return +1;
            }
            return v;
        });
        for (int i = 0; i < f.length; i++) {
            String name = f[i].getName();
            if (name.startsWith("check") && !mSkippedTests.contains(name)) {
                list.add(name);
            }
        }
        return list.toArray(new String[0]);
    }

    @Override
    public void command(Server server, String command, Server.Reader scanner, OutputStream out) {
        if (DEBUG) {
            System.out.println("Received <" + command + ">");
        }
        Server.Writer output = new Server.Writer(out);
        Command aCommand = mCommands.get(command);
        if (aCommand != null) {
            aCommand.process(this, server, output, scanner, out);
        }
        if (DEBUG) {
            System.out.println("Done with " + command);
        }
        output.println("DONE");
    }

    interface Command {
        void process(Activity activity, Server server,
                     Server.Writer writer, Server.Reader reader,
                     OutputStream out);
    }

    interface TestLayout {
        boolean apply(View view, String mode, int widthMeasureSpec, int heightMeasureSpec, FrameLayout.LayoutParams layoutParams, Fragment[] fragment);
    }
}
