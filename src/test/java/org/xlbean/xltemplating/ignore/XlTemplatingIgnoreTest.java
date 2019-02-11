package org.xlbean.xltemplating.ignore;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.core.TemplatingLifecycle;
import org.xlbean.xltemplating.core.impl.DefaultTemplatePreprocessor;

public class XlTemplatingIgnoreTest {

    /**
     * A blank line matches no files, so it can serve as a separator for
     * readability.
     */
    @Test
    public void blank() {

        String templateRootPath = "./unittest/01_blank/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignore = loader.load();
        System.out.println(ignore.getIgnores());

        assertThat(ignore.getIgnores().get(0).getPath(), is("aaa"));
        assertThat(ignore.getIgnores().get(1).getPath(), is("bbb"));
        assertThat(ignore.getIgnores().get(2).getPath(), is("/.xltmpignore"));
        assertThat(ignore.getIgnores().size(), is(3));
    }

    /**
     * A line starting with # serves as a comment. Put a backslash ("\") in front of
     * the first hash for patterns that begin with a hash.
     */
    @Test
    public void commentsAndEscapeComment() {

        String templateRootPath = "./unittest/02_commentsAndEscapeComment/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignore = loader.load();
        System.out.println(ignore.getIgnores());

        assertThat(
            ignore.getIgnores().get(0).getPath(),
            is("/#this_is_not_comment/and/should_start_from_#"));
        assertThat(
            ignore.getIgnores().get(1).getPath(),
            is("/this_back_slash/should_not_unescaped/here_\\_here"));
        assertThat(ignore.getIgnores().get(2).getPath(), is("/.xltmpignore"));
        assertThat(ignore.getIgnores().size(), is(3));
    }

    /**
     * Trailing spaces are ignored unless they are quoted with backslash ("\").
     */
    @Test
    public void trailingSpaces() {

        String templateRootPath = "./unittest/03_trailingSpaces/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignore = loader.load();
        System.out.println(ignore.getIgnores());

        assertThat(
            ignore.getIgnores().get(0).getPath(),
            is("/spaces_in_the_middle/of/a_line should/ a/remain"));
        assertThat(ignore.getIgnores().get(1).getPath(), is("trailing_spaces_should_be_trimmed-"));
        assertThat(
            ignore.getIgnores().get(2).getPath(),
            is("any_last_backslash_should_be_removed"));
        assertThat(ignore.getIgnores().get(3).getPath(), is("/.xltmpignore"));
        assertThat(ignore.getIgnores().size(), is(4));
    }

    /**
     * An optional prefix "!" which negates the pattern; any matching file excluded
     * by a previous pattern will become included again. It is not possible to
     * re-include a file if a parent directory of that file is excluded. Git doesn’t
     * list excluded directories for performance reasons, so any patterns on
     * contained files have no effect, no matter where they are defined. Put a
     * backslash ("\") in front of the first "!" for patterns that begin with a
     * literal "!", for example, "\!important!.txt".
     */
    @Test
    public void negates() {
        String templateRootPath = "./unittest/04_negates/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/04_negates")));
        assertThat(paths.get(1), is(Paths.get("./unittest/04_negates/foo")));
        assertThat(paths.get(2), is(Paths.get("./unittest/04_negates/foo/bar")));
        assertThat(paths.get(3), is(Paths.get("./unittest/04_negates/foo/bar/baz")));
        assertThat(paths.get(4), is(Paths.get("./unittest/04_negates/foo/bar/baz/baz.java")));
        assertThat(paths.get(5), is(Paths.get("./unittest/04_negates/foo/bar/baz/foo.java")));
        assertThat(paths.get(6), is(Paths.get("./unittest/04_negates/foo/bar/foo.java")));
        assertThat(paths.get(7), is(Paths.get("./unittest/04_negates/foo/foo.java")));
    }

    /**
     * If the pattern ends with a slash, it is removed for the purpose of the
     * following description, but it would only find a match with a directory. In
     * other words, foo/ will match a directory foo and paths underneath it, but
     * will not match a regular file or a symbolic link foo (this is consistent with
     * the way how pathspec works in general in Git).
     */
    @Test
    public void directoryMatch() throws Exception {
        String templateRootPath = "./unittest/05_directoryMatch/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/05_directoryMatch")));
        assertThat(paths.get(1), is(Paths.get("./unittest/05_directoryMatch/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/05_directoryMatch/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/05_directoryMatch/bar/barfoo/foo")));
        assertThat(paths.get(4), is(Paths.get("./unittest/05_directoryMatch/baz")));
        assertThat(paths.get(5), is(Paths.get("./unittest/05_directoryMatch/foo2")));
        assertThat(paths.get(6), is(Paths.get("./unittest/05_directoryMatch/foo2/bar")));
        assertThat(paths.get(7), is(Paths.get("./unittest/05_directoryMatch/foo2/foo")));
        assertThat(paths.get(8), is(Paths.get("./unittest/05_directoryMatch/foo3")));
    }

    /**
     * If the pattern does not contain a slash /, Git treats it as a shell glob
     * pattern and checks for a match against the pathname relative to the location
     * of the .gitignore file (relative to the toplevel of the work tree if not from
     * a .gitignore file).
     */
    @Test
    public void noSlash() {
        String templateRootPath = "./unittest/06_noSlash/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/06_noSlash")));
        assertThat(paths.get(1), is(Paths.get("./unittest/06_noSlash/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/06_noSlash/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/06_noSlash/baz")));
        assertThat(paths.get(4), is(Paths.get("./unittest/06_noSlash/foo2")));
        assertThat(paths.get(5), is(Paths.get("./unittest/06_noSlash/foo2/bar")));
        assertThat(paths.get(6), is(Paths.get("./unittest/06_noSlash/foo3")));
    }

    /**
     * Otherwise, Git treats the pattern as a shell glob: "*" matches anything
     * except "/", "?" matches any one character except "/" and "[]" matches one
     * character in a selected range. See fnmatch(3) and the FNM_PATHNAME flag for a
     * more detailed description.
     */
    @Test
    public void startsWithSlash() {
        String templateRootPath = "./unittest/07_startsWithSlash/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/07_startsWithSlash")));
        assertThat(paths.get(1), is(Paths.get("./unittest/07_startsWithSlash/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/07_startsWithSlash/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/07_startsWithSlash/bar/barfoo/foo")));
        assertThat(paths.get(4), is(Paths.get("./unittest/07_startsWithSlash/bar/foo")));
        assertThat(paths.get(5), is(Paths.get("./unittest/07_startsWithSlash/bar/foo/baz")));
        assertThat(paths.get(6), is(Paths.get("./unittest/07_startsWithSlash/bar/foo/foo")));
        assertThat(paths.get(7), is(Paths.get("./unittest/07_startsWithSlash/baz")));
        assertThat(paths.get(8), is(Paths.get("./unittest/07_startsWithSlash/foo2")));
        assertThat(paths.get(9), is(Paths.get("./unittest/07_startsWithSlash/foo2/bar")));
        assertThat(paths.get(10), is(Paths.get("./unittest/07_startsWithSlash/foo2/foo")));
        assertThat(paths.get(11), is(Paths.get("./unittest/07_startsWithSlash/foo3")));
        assertThat(paths.get(12), is(Paths.get("./unittest/07_startsWithSlash/foo3/foo")));
        assertThat(paths.get(13), is(Paths.get("./unittest/07_startsWithSlash/foo3/foo/baz")));
        assertThat(paths.get(14), is(Paths.get("./unittest/07_startsWithSlash/foo3/foo/foo")));
    }

    @Test
    public void slashInTheMiddle() {

        String templateRootPath = "./unittest/08_slashInTheMiddle/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/08_slashInTheMiddle")));
        assertThat(paths.get(1), is(Paths.get("./unittest/08_slashInTheMiddle/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/08_slashInTheMiddle/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/08_slashInTheMiddle/bar/barfoo/foo")));
        assertThat(paths.get(4), is(Paths.get("./unittest/08_slashInTheMiddle/bar/foo")));
        assertThat(paths.get(5), is(Paths.get("./unittest/08_slashInTheMiddle/bar/foo/baz")));
        assertThat(paths.get(6), is(Paths.get("./unittest/08_slashInTheMiddle/bar/foo/foo")));
        assertThat(paths.get(7), is(Paths.get("./unittest/08_slashInTheMiddle/baz")));
        assertThat(paths.get(8), is(Paths.get("./unittest/08_slashInTheMiddle/foo")));
        assertThat(paths.get(9), is(Paths.get("./unittest/08_slashInTheMiddle/foo/foo")));
        assertThat(paths.get(10), is(Paths.get("./unittest/08_slashInTheMiddle/foo2")));
        assertThat(paths.get(11), is(Paths.get("./unittest/08_slashInTheMiddle/foo2/bar")));
        assertThat(paths.get(12), is(Paths.get("./unittest/08_slashInTheMiddle/foo2/foo")));
        assertThat(paths.get(13), is(Paths.get("./unittest/08_slashInTheMiddle/foo3")));
        assertThat(paths.get(14), is(Paths.get("./unittest/08_slashInTheMiddle/foo3/foo")));
        assertThat(paths.get(15), is(Paths.get("./unittest/08_slashInTheMiddle/foo3/foo/baz")));
        assertThat(paths.get(16), is(Paths.get("./unittest/08_slashInTheMiddle/foo3/foo/foo")));
    }

    @Test
    public void asteriskFile() {

        String templateRootPath = "./unittest/09_asteriskFile/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/09_asteriskFile")));
        assertThat(paths.get(1), is(Paths.get("./unittest/09_asteriskFile/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/09_asteriskFile/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/09_asteriskFile/bar/barfoo/foo.java")));
        assertThat(paths.get(4), is(Paths.get("./unittest/09_asteriskFile/bar/foo")));
        assertThat(paths.get(5), is(Paths.get("./unittest/09_asteriskFile/bar/foo/baz.c")));
        assertThat(paths.get(6), is(Paths.get("./unittest/09_asteriskFile/bar/foo/foo.c")));
        assertThat(paths.get(7), is(Paths.get("./unittest/09_asteriskFile/baz")));
        assertThat(paths.get(8), is(Paths.get("./unittest/09_asteriskFile/foo")));
        assertThat(paths.get(9), is(Paths.get("./unittest/09_asteriskFile/foo/foo.java")));
        assertThat(paths.get(10), is(Paths.get("./unittest/09_asteriskFile/foo2")));
        assertThat(paths.get(11), is(Paths.get("./unittest/09_asteriskFile/foo2/bar.c")));
        assertThat(paths.get(12), is(Paths.get("./unittest/09_asteriskFile/foo2/foo.c")));
        assertThat(paths.get(13), is(Paths.get("./unittest/09_asteriskFile/foo3")));
        assertThat(paths.get(14), is(Paths.get("./unittest/09_asteriskFile/foo3/foo")));
        assertThat(paths.get(15), is(Paths.get("./unittest/09_asteriskFile/foo3/foo/foo.c")));
    }

    /**
     * A leading slash matches the beginning of the pathname. For example, "/*.c"
     * matches "cat-file.c" but not "mozilla-sha1/sha1.c".
     */
    @Test
    public void leadingSlash() {

        String templateRootPath = "./unittest/10_asteriskFileStartsWithSlash/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash")));
        assertThat(paths.get(1), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/bar/barfoo/foo.java")));
        assertThat(paths.get(4), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/bar/foo")));
        assertThat(paths.get(5), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/bar/foo/baz.c")));
        assertThat(paths.get(6), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/bar/foo/foo.c")));
        assertThat(paths.get(7), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/baz")));
        assertThat(paths.get(8), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo")));
        assertThat(paths.get(9), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo/baz.java")));
        assertThat(paths.get(10), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo/foo.java")));
        assertThat(paths.get(11), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo2")));
        assertThat(paths.get(12), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo3")));
        assertThat(paths.get(13), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo3/foo")));
        assertThat(paths.get(14), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo3/foo/baz.java")));
        assertThat(paths.get(15), is(Paths.get("./unittest/10_asteriskFileStartsWithSlash/foo3/foo/foo.c")));
    }

    /**
     * A leading "**" followed by a slash means match in all directories. For
     * example, "** /foo" matches file or directory "foo" anywhere, the same as
     * pattern "foo". "** /foo/bar" matches file or directory "bar" anywhere that is
     * directly under directory "foo".
     */
    @Test
    public void twoConsecutiveAsterisks() {

        String templateRootPath = "./unittest/11_twoConsecutiveAsterisks/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/11_twoConsecutiveAsterisks")));
        assertThat(paths.get(1), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/bar/barfoo/foo.java")));
        assertThat(paths.get(4), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/baz")));
        assertThat(paths.get(5), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/foo2")));
        assertThat(paths.get(6), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/foo2/bar.c")));
        assertThat(paths.get(7), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/foo2/foo.c")));
        assertThat(paths.get(8), is(Paths.get("./unittest/11_twoConsecutiveAsterisks/foo3")));
    }

    /**
     * A trailing "/**" matches everything inside. For example, "abc/**" matches all
     * files inside directory "abc", relative to the location of the .gitignore
     * file, with infinite depth.
     */
    @Test
    public void trailingTwoConsecutiveAsterisks() {

        String templateRootPath = "./unittest/12_trailingTwoConsecutiveAsterisks/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks")));
        assertThat(paths.get(1), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/bar/barfoo/foo.java")));
        assertThat(paths.get(4), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/bar/foo")));
        assertThat(paths.get(5), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/bar/foo/baz.c")));
        assertThat(paths.get(6), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/bar/foo/foo.c")));
        assertThat(paths.get(7), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/baz")));
        assertThat(paths.get(8), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo")));
        assertThat(paths.get(9), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo/baz.java")));
        assertThat(paths.get(10), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo/foo.java")));
        assertThat(paths.get(11), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo2")));
        assertThat(paths.get(12), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo2/bar.c")));
        assertThat(paths.get(13), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo2/foo.c")));
        assertThat(paths.get(14), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo3")));
        assertThat(paths.get(15), is(Paths.get("./unittest/12_trailingTwoConsecutiveAsterisks/foo3/foo")));
    }

    /**
     * FROM Git:
     *
     * A slash followed by two consecutive asterisks then a slash matches zero or
     * more directories. For example, "a/** /b" matches "a/b", "a/x/b", "a/x/y/b"
     * and so on.
     * 
     * DIFFERENCE:
     * 
     * A slash followed by two consecutive asterisks then a slash matches ONE or
     * more directories.
     * 
     */
    @Test
    public void slashAfterTwoConsecutiveAsterisks() {

        String templateRootPath = "./unittest/13_twoConsecutiveAsterisksInTheMiddle/";
        XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(
            Paths.get(templateRootPath),
            Charset.forName("utf-8"));

        XlTemplatingIgnores ignores = loader.load();
        System.out.println(ignores.getIgnores());

        TemplatingContext context = new TemplatingContext.Builder()
            .templateRootDir(Paths.get(templateRootPath))
            .preprocessor(new DefaultTemplatePreprocessor())
            .build();
        TemplatingLifecycle lifecycle = new TemplatingLifecycle();

        List<Path> paths = lifecycle.getTargetTemplates(context);
        paths.forEach(p -> System.out.println(Files.isDirectory(p) + "\t" + p));

        assertThat(paths.get(0), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle")));
        assertThat(paths.get(1), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/bar")));
        assertThat(paths.get(2), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/bar/barfoo")));
        assertThat(paths.get(3), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/bar/barfoo/foo.java")));
        assertThat(paths.get(4), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/bar/foo")));
        assertThat(paths.get(5), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/bar/foo/baz.c")));
        assertThat(paths.get(6), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/bar/foo/foo.c")));
        assertThat(paths.get(7), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/baz")));
        assertThat(paths.get(8), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo")));
        assertThat(paths.get(9), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo/bar")));
        assertThat(paths.get(10), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo/bar/baz")));
        assertThat(
            paths.get(11),
            is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo/bar/baz/foo.java")));
        assertThat(paths.get(12), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo/bar/foo.java")));
        assertThat(paths.get(13), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo/baz.java")));
        assertThat(paths.get(14), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo/foo.java")));
        assertThat(paths.get(15), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo2")));
        assertThat(paths.get(16), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo2/bar.c")));
        assertThat(paths.get(17), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo2/foo.c")));
        assertThat(paths.get(18), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo3")));
        assertThat(paths.get(19), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo3/foo")));
        assertThat(paths.get(20), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo3/foo/baz.java")));
        assertThat(paths.get(21), is(Paths.get("./unittest/13_twoConsecutiveAsterisksInTheMiddle/foo3/foo/foo.c")));
    }

}
