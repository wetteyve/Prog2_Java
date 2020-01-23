import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;

import java.util.List;

public class MusicJukeBoxTest {

	private JukeBox jukeBox = null;

	@Before
	public void setUp() throws Exception {
		jukeBox = new MusicJukeBox();
	}

	@Test(expected = JukeBoxException.class)
	public void testPlayOfNoneExistingSong() throws Exception{
		// test empty music juke box
        jukeBox.playTitle("NoneExistingTitle");
	}

	@Test
	public void testGetPlayList1() {
	    Song track = mock(Song.class);
	    when(track.getTitle()).thenReturn("title");
		jukeBox.addSong(track);

		assertEquals(1, jukeBox.getPlayList().size());
		verify(track, times(1)).getTitle();
	}

    @Test
    public void testGetPlayList2() {
        Song track1 = mock(Song.class);
        when(track1.getTitle()).thenReturn("title1");
        jukeBox.addSong(track1);
        Song track2 = mock(Song.class);
        when(track2.getTitle()).thenReturn("title2");
        jukeBox.addSong(track2);
        Song track3 = mock(Song.class);
        when(track3.getTitle()).thenReturn("title3");
        jukeBox.addSong(track3);
        Song track4 = mock(Song.class);
        when(track4.getTitle()).thenReturn("title4");
        jukeBox.addSong(track4);
        Song track5 = mock(Song.class);
        when(track5.getTitle()).thenReturn("title5");
        jukeBox.addSong(track5);

        assertEquals(5, jukeBox.getPlayList().size());
    }

	@Test
	public void testPlay() {
		jukeBox.addSong(new SongStub("d"));
		jukeBox.playTitle("d");

		Song song = jukeBox.getActualSong();
		assertEquals("d", song.getTitle());

		assertTrue(song.isPlaying());
	}

	@Test(expected = JukeBoxException.class)
	public void testPlayOfAlreadyPlayingSong() {
	    Song track = mock(Song.class);
	    when(track.getTitle()).thenReturn("d");
        doThrow(new JukeBoxException("No Song found with title 'd'")).when(track).start();
		jukeBox.addSong(track);
		jukeBox.playTitle("d");
        jukeBox.playTitle("d");
	}

	@Test
    public void testPlayMock() {
	    Song track = mock(Song.class);
        InOrder inOrder = inOrder(track);
	    when(track.getTitle()).thenReturn("title");
	    jukeBox.addSong(track);
	    jukeBox.playTitle("title");
        inOrder.verify(track).getTitle();
    }

    @Test
    public void testArgumentMatcher(){
        JukeBox mocky = mock(JukeBox.class);
        Song track = mock(Song.class);
        List<Song> mockedlist = mock(List.class);
        when(mockedlist.get(anyInt())).thenAnswer((invocationOnMock) -> track);
        when(mocky.getPlayList()).thenReturn(mockedlist);

        assertEquals((mocky.getPlayList().get(0)), track);
        assertEquals((mocky.getPlayList().get(1)), track);
        assertEquals((mocky.getPlayList().get(2)), track);
        assertEquals((mocky.getPlayList().get(3)), track);
        assertEquals((mocky.getPlayList().get(4)), track);

    }

}
