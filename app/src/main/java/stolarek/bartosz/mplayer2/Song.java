package stolarek.bartosz.mplayer2;

public class Song {
    private String artist, name, url;

    public Song(String name, String artist, String url) {
        this.name = name;
        this.url = url;

        if (artist.equals("<unknown>"))
            this.artist = "Unknown Artist";
        else
            this.artist = artist;

    }

    public String getArtist() {
        return artist;
    }
    public String getName() {
        return name;
    }
    public String getUrl() { return url; }
}
