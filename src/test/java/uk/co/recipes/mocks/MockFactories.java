/**
 * 
 */
package uk.co.recipes.mocks;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.co.recipes.CanonicalItem;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.persistence.EsItemFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class MockFactories {

    public static EsItemFactory mockItemFactory() {
    	try {
            final EsItemFactory iff = mock( EsItemFactory.class );

            when( iff.get( anyString() )).thenAnswer( new Answer<Optional<ICanonicalItem>>() {

				@Override
				public Optional<ICanonicalItem> answer( InvocationOnMock invocation) {
					final String name = (String) invocation.getArguments()[0];

					final ICanonicalItem item = new CanonicalItem(name);
					item.setId( Math.abs( name.hashCode() ));  // Yuk!
					return Optional.of(item);
				}} );

            return iff;
    	}
    	catch (IOException e) {
    		throw Throwables.propagate(e);
    	}
    }
}
