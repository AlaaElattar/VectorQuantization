import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * @author Alaa Mahmoud Ebrahim 20190105
 * @author Omar Khaled Al Haj 20190351
 * */
class vector {
    int width ;
    int height ;
    double [][] data ;
    public vector(){}
    public vector(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new double [height][width];
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public double[][] getData() {
        return data;
    }
    public void setData(double[][] data) {
        this.data = data;
    }
}

class split_element{
    vector value;
    ArrayList < vector > assoicated = new ArrayList <> ( );

    public split_element ( ) {}
    public split_element ( vector value , ArrayList < vector > assoicated ) {
        this.value = value;
        this.assoicated = assoicated;
    }
    public vector getValue ( ) {
        return value;
    }
    public void setValue ( vector value ) {
        this.value = value;
    }
    public ArrayList < vector > getAssoicated ( ) {
        return assoicated;
    }
}

public class VQ {

    public int[][] originalImage;
    public String nameOfCodeBook = "CodeBook.txt";
    public String nameOfDecompress = "Decompressed.jpg";
    Scanner sc;
    Formatter out;

    public static int[][] readImage ( String filePath ) {
        File file = new File ( filePath );
        BufferedImage image = null;
        try{
            image = ImageIO.read ( file );
        } catch (IOException e){
            e.printStackTrace ( );
        }
        int width = image.getWidth ( );
        int height = image.getHeight ( );
        int[][] pixels = new int[ height ][ width ];
        for ( int x = 0 ; x < width ; x++ ) {
            for ( int y = 0 ; y < height ; y++ ) {
                int rgb = image.getRGB ( x ,y );
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;

                pixels[ y ][ x ] = r;
            }
        }
        return pixels;
    }
    public static void writeImage ( int[][] pixels ,String outPath ,int width ,int height ) {
        File fileOut = new File ( outPath );
        BufferedImage image2 = new BufferedImage ( width ,height ,BufferedImage.TYPE_INT_RGB );

        for ( int x = 0 ; x < width ; x++ ) {
            for ( int y = 0 ; y < height ; y++ ) {
                image2.setRGB ( x ,y ,(pixels[ y ][ x ] << 16) | (pixels[ y ][ x ] << 8) | (pixels[ y ][ x ]) );
            }
        }
        try{
            ImageIO.write ( image2 ,"jpg" ,fileOut );
        } catch (IOException e){
            e.printStackTrace ( );
        }
    }
    ArrayList< vector > BuildVectors ( int[][] originalImage ,vector[][] vectors ,int numOfRows ,int numOfCols ,int widthOfVector ,int heightOfVector ) {

        ArrayList< vector > AllVectors = new ArrayList<> ( );
        vector curVector = new vector ( widthOfVector ,heightOfVector );

        for ( int i = 0 ; i < originalImage.length ; i += heightOfVector ) {
            for ( int j = 0 ; j < originalImage[ 0 ].length ; j += widthOfVector ) {
                int x = i;
                int z = j;
                curVector = new vector ( widthOfVector ,heightOfVector );
                for ( int n = 0 ; n < heightOfVector ; n++ ) {
                    for ( int m = 0 ; m < widthOfVector ; m++ ) {
                        curVector.data[ n ][ m ] = originalImage[ x ][ z++ ];
                    }
                    x++;
                    z = j;
                }
                AllVectors.add ( curVector );
            }
        }
        int indx = 0;
        for ( int i = 0 ; i < numOfRows ; i++ ) {
            for ( int j = 0 ; j < numOfCols ; j++ ) {
                vectors[ i ][ j ] = AllVectors.get ( indx++ );
            }
        }
        return AllVectors;
    }
    int IndexOfMinDistance ( ArrayList< Double > distance_difference ) {
        double min_diff = distance_difference.get ( 0 ); // assume first element is the min
        int indx = 0;

        for ( int i = 1 ; i < distance_difference.size ( ) ; i++ ) {
            if ( distance_difference.get ( i ) < min_diff ) {
                min_diff = distance_difference.get ( i );
                indx = i;
            }

        }
        return indx;
    }
    ArrayList<vector> Associate ( ArrayList<vector> split ,ArrayList<vector> data ) {
        ArrayList<split_element> Split = new ArrayList<> ( );
        ArrayList<vector> Averages = new ArrayList<> ( );
        int width = data.get ( 0 ).width;
        int height = data.get ( 0 ).height;

        for ( int i = 0 ; i < split.size ( ) ; i++ )  // inilialization
        {
            split_element initial = new split_element ( );
            initial.setValue ( split.get ( i ) );
            Split.add ( initial );
        }

        for ( int i = 0 ; i < data.size ( ) ; i++ ) // Associate data
        {
            vector current = data.get ( i );
            ArrayList<Double> distance_difference = new ArrayList<> ( );

            // PrintVector(data.get(i));

            for ( int j = 0 ; j < split.size ( ) ; j++ ) {
                double total_diff = 0;

                for ( int w = 0 ; w < width ; w++ ) {
                    for ( int h = 0 ; h < height ; h++ ) {
                        double value = current.data[ w ][ h ] - split.get ( j ).data[ w ][ h ];
                        double distanc_diff = Math.pow ( value ,2 );
                        total_diff += distanc_diff;
                    }
                }

                //  System.out.println("Total diff = " + total_diff);
                distance_difference.add ( total_diff );

            }

            int indx = IndexOfMinDistance ( distance_difference );

            ArrayList<vector> cur_associated = Split.get ( indx ).getAssoicated ( );

            cur_associated.add ( current );

            split_element New = new split_element ( Split.get ( indx ).getValue ( ) ,cur_associated );

            Split.set ( indx ,New );

        }


        for ( int i = 0 ; i < Split.size ( ) ; i++ ) // calculate average for the associated values
        {
            int arraysize = Split.get ( i ).getAssoicated ( ).size ( );
            vector avg = new vector ( width ,height );

            for ( int w = 0 ; w < width ; w++ ) {
                for ( int h = 0 ; h < height ; h++ ) {
                    double total = 0;

                    for ( int j = 0 ; j < arraysize ; j++ ) {
                        total += Split.get ( i ).getAssoicated ( ).get ( j ).data[ w ][ h ];
                    }

                    avg.data[ w ][ h ] = total / arraysize;
                }

            }

            Averages.add ( avg );

        }

        return Averages;
    }
    ArrayList<vector> SplitVectors ( ArrayList<vector> Averages ,ArrayList<vector> data ,int numoflevels ){
        int width = Averages.get (0).width;
        int height = Averages.get (0).height;

        for ( int i = 0 ; i < Averages.size ( ) ; i++ ) {
            if ( Averages.size ( ) < numoflevels ) {

                ArrayList<vector> split = new ArrayList<> ( );

                for ( int j = 0 ; j < Averages.size ( ) ; j++ ) {
                    vector left = new vector (width ,height);
                    vector right = new vector (width ,height);

                    for ( int w = 0 ; w < width ; w++ ) {
                        for ( int h = 0 ; h < height ; h++ ) {
                            int cast = ( int ) Averages.get (j).data[ w ][ h ];

                            left.data[ w ][ h ] = cast;
                            right.data[ w ][ h ] = cast + 1;

                        }

                    }

                    split.add (left);
                    split.add (right);
                }

                Averages.clear ( );

                Averages = Associate (split ,data);

                i = 0;

            } else
                break;

        }

        return Averages;
    }
    ArrayList<vector> Modify ( ArrayList<vector> prev_Averages ,ArrayList<vector> new_Averages ,ArrayList<vector> data ) {
        while ( true ) {
            int width = new_Averages.get (0).width;
            int height = new_Averages.get (0).height;
            int totaldiff = 0;
            int avgdiff = 0;

            for ( int i = 0 ; i < new_Averages.size ( ) ; i++ ) {
                double DiffrenceOf2Vectors = 0;

                for ( int w = 0 ; w < width ; w++ ) {
                    for ( int h = 0 ; h < height ; h++ ) {
                        DiffrenceOf2Vectors += Math.abs (prev_Averages.get (i).data[ w ][ h ] - new_Averages.get (i).data[ w ][ h ]);
                    }
                }

                totaldiff += DiffrenceOf2Vectors;
            }

            avgdiff = totaldiff / prev_Averages.size ( );

            if ( avgdiff < 0.0001 ) {
                break;
            }else {
                prev_Averages = new_Averages;
                new_Averages = Associate (new_Averages ,data);
            }

        }

        return new_Averages;

    }
    public void open_file ( String FileName ) {
        try {
            sc = new Scanner ( new File ( FileName ) );
        } catch (Exception e){
            e.printStackTrace ();
        }
    }
    public void close_file_Sc ( ) {    //close for Scanner
        sc.close ( );
    }
    public void openfile ( String pass ) {
        try {
            out = new Formatter ( pass );
        } catch (Exception e){
            e.printStackTrace (  );
        }
    }
    public void
    closeFileF ( ) {    //close for Formatter
        out.close ( );
    }
    void write ( String code ) {
        out.format ( "%s" ,code );
        out.format ( "%n" );
        out.flush ( );
    }
    void writeCodeBook ( ArrayList < vector > codeBook ,int[][] compressedImg ) {
        openfile ( nameOfCodeBook );
        String codeBookSize = "" + codeBook.size ( );
        String WidthOfBlock = "" + codeBook.get ( 0 ).width;
        String heightOfBlock = "" + codeBook.get ( 0 ).height;

        write ( codeBookSize );
        write ( WidthOfBlock );
        write ( heightOfBlock );

        for ( int i = 0 ; i < codeBook.size ( ) ; i++ ) {
            for ( int w = 0 ; w < codeBook.get ( i ).width ; w++ ) {
                String row = "";
                for ( int h = 0 ; h < codeBook.get ( i ).height ; h++ ) {
                    row += codeBook.get ( i ).data[ w ][ h ] + " ";
                }
                write ( row );
            }
        }

        String compressedImgHeight = "" + compressedImg.length;
        write ( compressedImgHeight );
        String compressedImgWidth = "" + compressedImg[ 0 ].length;
        write ( compressedImgWidth );

        for ( int i = 0 ; i < compressedImg.length ; i++ ) {
            String row = "";

            for ( int j = 0 ; j < compressedImg[ 0 ].length ; j++ ) {
                row += compressedImg[ i ][ j ] + " ";
            }
            write ( row );
        }
        closeFileF ( );
    }
    void CompressImage ( ArrayList< vector > codeBook ,vector[][] vectors ) {
        int Rows = vectors.length;
        int Cols = vectors[ 0 ].length;
        int[][] compress_Image = new int[ Rows ][ Cols ];

        for ( int i = 0 ; i < Rows ; i++ ) {
            for ( int j = 0 ; j < Cols ; j++ ) {
                vector currentVector = vectors[ i ][ j ];
                ArrayList< Double > distanceDifference = new ArrayList<> ( );

                for ( int k = 0 ; k < codeBook.size ( ) ; k++ ) {
                    double total_diff = 0;

                    for ( int w = 0 ; w < codeBook.get ( 0 ).width ; w++ ) {
                        for ( int h = 0 ; h < codeBook.get ( 0 ).height ; h++ ) {
                            double value = currentVector.data[ w ][ h ] - codeBook.get ( k ).data[ w ][ h ];
                            double distanc_diff = Math.pow ( value ,2 );
                            total_diff += distanc_diff;
                        }
                    }

                    distanceDifference.add ( total_diff );
                }

                int index = IndexOfMinDistance ( distanceDifference );
                compress_Image[ i ][ j ] = index;
            }
        }
        writeCodeBook ( codeBook ,compress_Image );
    }
    void Quantization ( int numoflevels ,ArrayList<vector> data ,int widthOfVector ,int heightOfVector ,vector[][] vectors ,int numOfRows ,int numOfCols ) {
        ArrayList<vector> Averages = new ArrayList<> ( );
        // initalize first avg
        vector first_avg = new vector ( widthOfVector ,heightOfVector );

        for ( int w = 0 ; w < widthOfVector ; w++ ) {
            for ( int h = 0 ; h < heightOfVector ; h++ ) {
                double total = 0;

                for ( int i = 0 ; i < data.size ( ) ; i++ ) {
                    total += data.get ( i ).data[ w ][ h ];

                }

                first_avg.data[ w ][ h ] = total / data.size ( );

            }

        }

        Averages.add ( first_avg );
        Averages = SplitVectors ( Averages ,data ,numoflevels );
        ArrayList<vector> prev_Averages = Averages;
        ArrayList<vector> new_Averages = Associate ( Averages ,data );
        new_Averages = Modify ( prev_Averages ,new_Averages ,data );


        ArrayList<vector> codeBook = new ArrayList<> ( );

        for ( int i = 0 ; i < new_Averages.size ( ) ; i++ ) {
            codeBook.add ( new_Averages.get ( i ) );
        }


        int index = 0;


        for ( int i = 0 ; i < widthOfVector ; i++ ) {
            for ( int j = 0 ; j < numOfCols ; j++ ) {
                vectors[ i ][ j ] = data.get ( index++ );
            }
        }

        CompressImage ( codeBook ,vectors );

    }
    int[][] Reconstruct ( ArrayList < vector > codeBook , int[][] compressedImg ) {
        open_file ( nameOfCodeBook );
        int codeBookSize = Integer.parseInt ( sc.nextLine ( ) );
        int WidthOfBlock = Integer.parseInt ( sc.nextLine ( ) );
        int heightOfBlock = Integer.parseInt ( sc.nextLine ( ) );

        for ( int i = 0 ; i < codeBookSize ; i++ ) {
            vector currentVector = new vector ( WidthOfBlock , heightOfBlock );

            for ( int w = 0 ; w < WidthOfBlock ; w++ ) {
                String row = sc.nextLine ( );
                String[] elements = row.split ( " " );

                for ( int h = 0 ; h < heightOfBlock ; h++ ) {
                    currentVector.data[ w ][ h ] = Double.parseDouble ( elements[ h ] );
                }
            }
            codeBook.add ( currentVector );
        }

        int compressedImgHeight = Integer.parseInt ( sc.nextLine ( ) );
        int compressedImgWidth = Integer.parseInt ( sc.nextLine ( ) );
        compressedImg = new int[ compressedImgHeight ][ compressedImgWidth ];

        for ( int i = 0 ; i < compressedImg.length ; i++ ) {
            String line = sc.nextLine ( );
            String[] row = line.split ( " " );

            for ( int j = 0 ; j < compressedImg[ 0 ].length ; j++ ) {
                compressedImg[ i ][ j ] = Integer.parseInt ( row[ j ] );
            }
        }
        close_file_Sc ( );
        return compressedImg;
    }
    void Decompress ( ) {
        ArrayList < vector > codeBook = new ArrayList < vector > ( );
        int[][] comp_image = new int[ 1 ][ 1 ];
        comp_image = Reconstruct ( codeBook , comp_image );
        int[][] Decomp_image = new int[ originalImage.length ][ originalImage[ 0 ].length ];

        for ( int i = 0 ; i < comp_image.length ; i++ ) {
            for ( int j = 0 ; j < comp_image[ 0 ].length ; j++ ) {
                vector cur = new vector ( );
                cur = codeBook.get ( comp_image[ i ][ j ] );

                int cornerx = i * cur.height;
                int cornery = j * cur.width;

                for ( int h = 0 ; h < cur.height ; h++ ) {
                    for ( int w = 0 ; w < cur.width ; w++ ) {
                        Decomp_image[ cornerx + h ][ cornery + w ] = (int) cur.data[ h ][ w ];
                    }
                }
            }
        }
        writeImage ( Decomp_image , nameOfDecompress , Decomp_image[ 0 ].length , Decomp_image.length );
    }
}

