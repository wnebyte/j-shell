package com.github.wnebyte.jcli;

import com.github.wnebyte.jcli.di.DependencyContainer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class InstanceTrackerTest {

    public InstanceTrackerTest() { }

    @Test
    public void test00() {
        InstanceTracker tracker = new InstanceTracker(new DependencyContainer());
        tracker.add(this);
        Object object = tracker.get(this.getClass());
        Assert.assertSame(this, object);
    }

    @Test
    public void test01() {
        InstanceTracker tracker = new InstanceTracker(new DependencyContainer());
        Object object = tracker.get(this.getClass());
        Assert.assertNotSame(this, object);
    }

    @Test
    public void test02() {
        InstanceTracker tracker = new InstanceTracker(new DependencyContainer());
        tracker.addAll(Arrays.asList(
                new InstanceTrackerTest(),
                new InstanceTrackerTest(),
                new InstanceTrackerTest()
        ));
        int size = tracker.size();
        Assert.assertEquals(1, size);
    }
}