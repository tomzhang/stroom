package stroom.refdata.lmdb;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import stroom.xml.event.EventListBuilder;
import stroom.xml.event.EventListBuilderFactory;
import stroom.xml.event.np.NPEventList;

public class TestNPEventListSerde {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestNPEventListSerde.class);


    @Test
    public void serializeDeserialize() {

        NPEventList npEventListInput = createEventList();

        NPEventListSerde serde = NPEventListSerde.instance();

        byte[] bytes = serde.serialize(npEventListInput);

        LOGGER.info("bytes.length Kryo {}", bytes.length);

        NPEventList npEventListOuput = serde.deserialize(bytes);

        Assertions.assertThat(npEventListInput).isEqualTo(npEventListOuput);
        Assertions.assertThat(npEventListInput.hashCode()).isEqualTo(npEventListOuput.hashCode());
    }

    private static NPEventList createEventList() {
        final EventListBuilder builder = EventListBuilderFactory.createBuilder();
        for (int i = 0; i < 100; i++) {
            try {
                builder.startDocument();
                builder.startPrefixMapping("t", "testuri");
                builder.startElement("testuri", "test", "test", null);
                String str = "testChars" + i;
                builder.characters(str.toCharArray(), 0, str.length());
                builder.endElement("testuri", "test", "test");
                builder.endDocument();
            } catch (final SAXException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        final NPEventList eventList = (NPEventList) builder.getEventList();
        builder.reset();

        return eventList;
    }

}