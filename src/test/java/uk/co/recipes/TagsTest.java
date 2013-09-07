/**
 * 
 */
package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.testng.annotations.Test;

import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.tags.ChiliTags;
import uk.co.recipes.tags.MeatAndFishTags;
import uk.co.recipes.tags.NationalCuisineTags;
import uk.co.recipes.tags.TagUtils;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class TagsTest {

	@Test
	public void testStyles() {
		final ICanonicalItem item = new CanonicalItem("Possum");
		item.addTag( NationalCuisineTags.ASIAN );
		item.addTag( MeatAndFishTags.MEAT );
		item.addTag( MeatAndFishTags.RED_MEAT );
		item.addTag( ChiliTags.SCOVILLE );
		item.addTag( ChiliTags.HEAT_5 );

		final List<String> displayTags = item.getTagNamesForDisplay();
		assertThat( displayTags.toString(), is("[Asian, Heat 5, Meat, Red Meat, Scoville]"));

		int i = 0;
		assertThat( TagUtils.getStyle( displayTags.get(i++) ), is("label-NationalCuisineTags"));
		assertThat( TagUtils.getStyle( displayTags.get(i++) ), is("label-ChiliTags"));
		assertThat( TagUtils.getStyle( displayTags.get(i++) ), is("label-MeatAndFishTags"));
		assertThat( TagUtils.getStyle( displayTags.get(i++) ), is("label-MeatAndFishTags"));
		assertThat( TagUtils.getStyle( displayTags.get(i++) ), is("label-ChiliTags"));
	}
}
