/*
 * #%L
 * SlidingMenuDemo
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 Paul Grime
 * %%
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
 * #L%
 */
package easy.lib;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * A HorizontalScrollView (HSV) implementation that disallows touch events (so no scrolling can be done by the user).
 * 
 * This HSV MUST contain a single ViewGroup as its only child, and this ViewGroup will be used to display the children Views
 * passed in to the initViews() method.
 */
public class MyHorizontalScrollView extends HorizontalScrollView {
	ViewGroup parent;
	View[] children;
	
    public MyHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyHorizontalScrollView(Context context) {
        super(context);
        init(context);
    }

    void init(Context context) {
        // remove the fading as the HSV looks better without it
        setHorizontalFadingEdgeEnabled(false);
        setVerticalFadingEdgeEnabled(false);
    }

    /**
     * @param children
     *            The child Views to add to parent.
     * @param menuWidth
     *            A para to set the view width of menu.
     */
    public void initViews(View[] children) {
        // A ViewGroup MUST be the only child of the HSV
        parent = (ViewGroup) getChildAt(0);
        this.children = children;
       
        // Add all the children, but add them invisible so that the layouts are calculated, but you can't see the Views
        for (int i = 0; i < children.length; i++) {
            children[i].setVisibility(View.INVISIBLE);
            parent.addView(children[i]);
        }
    }

    public void setLayout(int h, final int[] menuWidth, final int index) {
        parent.removeViewsInLayout(0, children.length);

        // Add each view in turn, and apply the specified width and height.
        for (int i = 0; i < children.length; i++) {
            children[i].setVisibility(View.VISIBLE);
            parent.addView(children[i], menuWidth[i], h);
        }
        
        // For some reason we need to post this action, rather than call immediately.
        // If we try immediately, it will not scroll.
        new Handler().post(new Runnable() {
            @Override
            public void run() {
            	int x;
            	if (index == 0) x = 0;
            	else if (index == 1) x = menuWidth[0];
            	else x = menuWidth[0] + menuWidth[2];
            	MyHorizontalScrollView.this.smoothScrollTo(x, 0);
            }
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Do not allow touch events.
        return false;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Do not allow touch events.
        return false;
    }
}
