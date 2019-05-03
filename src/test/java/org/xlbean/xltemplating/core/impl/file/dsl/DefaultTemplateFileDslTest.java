package org.xlbean.xltemplating.core.impl.file.dsl;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.xlbean.xltemplating.core.impl.file.dsl.DefaultTemplateFileDslConfig.OutputConfig;

public class DefaultTemplateFileDslTest {

    @Test
    public void evaluate() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> d1 = new HashMap<>();
        d1.put("dirname", "dir");
        list.add(d1);
        Map<String, Object> d2 = new HashMap<>();
        d2.put("dirname", "dir2");
        list.add(d2);

        Map<String, Object> excel = new HashMap<>();
        excel.put("list", list);

        String dsl = "iterator {list}; output{dir{\"test${dirname}\"}; filename{'testfile'}}";

        DefaultTemplateFileDslConfig script = new DefaultTemplateFileDsl().evaluate(dsl, excel);

        assertThat(script.getIterator(), is(list));
        OutputConfig outputConfig = script.getOutputConfig(d1);
        assertThat(outputConfig.getDir(), is("testdir"));
        assertThat(outputConfig.getFilename(), is("testfile"));
        OutputConfig outputConfig2 = script.getOutputConfig(d2);
        assertThat(outputConfig2.getDir(), is("testdir2"));
        assertThat(outputConfig2.getFilename(), is("testfile"));
    }

    @Test
    public void evaluate_null() {
        DefaultTemplateFileDslConfig script = new DefaultTemplateFileDsl().evaluate(null, null);

        assertThat(script, is(instanceOf(DefaultTemplateFileDslConfig.class)));

    }
}
