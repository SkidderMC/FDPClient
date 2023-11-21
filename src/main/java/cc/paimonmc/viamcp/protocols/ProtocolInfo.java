package cc.paimonmc.viamcp.protocols;

public class ProtocolInfo {
    private final String name;
    private final String description;
    private final String releaseDate;

    public ProtocolInfo(final String name, final String description, final String releaseDate) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
}
