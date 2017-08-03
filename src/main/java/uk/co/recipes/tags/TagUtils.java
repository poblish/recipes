/**
 *
 */
package uk.co.recipes.tags;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import uk.co.recipes.api.ITag;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 *
 * @author andrewregan
 */
public final class TagUtils {

    @SuppressWarnings("serial")
    static final Map<String,ITag> ALL_TAGS = new LinkedHashMap<String,ITag>() {{
        for (CommonTags each : CommonTags.values()) put(each.name(), each);
        for (FlavourTags each : FlavourTags.values()) put(each.name(), each);
        for (NationalCuisineTags each : NationalCuisineTags.values()) put(each.name(), each);
        for (RecipeTags each : RecipeTags.values()) put(each.name(), each);
        for (MeatAndFishTags each : MeatAndFishTags.values()) put(each.name(), each);
        for (ChiliTags each : ChiliTags.values()) put(each.name(), each);
    }};

    private TagUtils() {
    }

    private static final Function<Entry<ITag,Serializable>,ITag> ENTRY_KEYS = Entry::getKey;

    private static final Function<Entry<ITag,Serializable>,String> TAG_NAMES_TITLECASE = input -> formatTagName(input.getKey());

    private static final Predicate<Entry<ITag,Serializable>> ALL_ACTIVATED = TagUtils::isActivated;

    public static Predicate<Entry<ITag,Serializable>> findActivated(final ITag... inTags) {
        final Collection<ITag> tagsColl = Arrays.asList(inTags);
        return inEntry -> tagsColl.contains(inEntry.getKey()) && isActivated(inEntry);
    }

    private static boolean isActivated(final Entry<ITag,Serializable> inEntry) {
        if (inEntry.getValue() instanceof Boolean) {
            return ((Boolean) inEntry.getValue());
        }

        final String strVal = (String) inEntry.getValue();
        if (strVal == null || strVal.isEmpty() || strVal.equalsIgnoreCase("false")) {
            return false;
        }

        return true;  // 'true' or any non-Boolean representation is *fine* !
    }

    public static Predicate<Entry<ITag,Serializable>> findActivated() {
        return ALL_ACTIVATED;
    }

    public static Function<Entry<ITag,Serializable>,ITag> entryKeys() {
        return ENTRY_KEYS;
    }

    public static Function<Entry<ITag,Serializable>,String> tagNamesTitleCase() {
        return TAG_NAMES_TITLECASE;
    }

    public static ITag forName(final String inName) {
        return checkNotNull(ALL_TAGS.get(inName), "No Tag registered with name '" + inName + "'");
    }

    public static String getStyle(final String inName) {
        return getStyle(ALL_TAGS.get(inName.toUpperCase().replace(' ', '_')));
    }

    public static String getStyle(final ITag inTag) {
        return (inTag != null) ? "label-" + inTag.getClass().getSimpleName() : "label-primary";
    }

    public static List<ITag> findTagsByName(String inName) {
        if (inName == null || inName.isEmpty()) {
            return Collections.emptyList();
        }

        List<ITag> results = Lists.newArrayList();

        final String lcaseName = inName.toLowerCase();

        for (Entry<String,ITag> each : ALL_TAGS.entrySet()) {
            if (each.getKey().toLowerCase().equals(lcaseName)) {  // FIXME This is pretty lame
                results.add(each.getValue());
            }
        }

        return results;
    }

    public static String formatTagName(final ITag inTag) {
        final StringBuilder sb = new StringBuilder();  // Yuk, but Guava CaseFormat just didn't cut it

        for (String word : Splitter.on('_').split(inTag.toString())) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }

        return sb.toString();
    }

    public static Comparator<ITag> comparator() {
        return (o1, o2) -> {
            // Yuk, FIXME
            return ComparisonChain.start().compare(o1.toString(), o2.toString()).result();
        };
    }
}