
package util;
public class LyricBlock
{
    public static enum BlockType { STROFA, RIT }
    
    private String text;
    private BlockType type;

    public LyricBlock(String text, BlockType type)
    {
        this.text = text;
        this.type = type;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public BlockType getType() {
        return type;
    }

    public void setType(BlockType type) {
        this.type = type;
    }
}
