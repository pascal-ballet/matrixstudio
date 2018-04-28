package matrixstudio.ui;

import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public class SimpleMatrixRenderer implements MatrixRenderer {

        @Override
	public void render(GC gc, RendererContext context, Matrix matrix, int mouseZ) {
		ImageData imageData = null;

        int k = mouseZ; //(int)Math.floor(matrix.getSizeX()/2);
        int sizeX = matrix.safeGetSizeXValue();
        int sizeY = matrix.safeGetSizeYValue();
        if ( matrix instanceof MatrixInteger ) {
			MatrixInteger matrixInteger = (MatrixInteger) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(sizeX, sizeY, 32, palette);
			if(matrixInteger.isARGB() == false) {
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						Integer value = matrixInteger.getMatrix()[k * sizeX * sizeY + j * sizeX + i];
						int r, g, b;
						g = (value >> 8) & 255;
						r = (value & 255);
						b = (value >> 16) & 255;
						if(matrixInteger.isRainbow() == true) {
							g = (g *5843) & 255;
							r = (r * 5843) & 255;
							b = (b * 5843) & 255;
						}
						RGB rgb = new RGB(r, g, b);
						imageData.setPixel(i, sizeY - j - 1, palette.getPixel(rgb));
					}
				}
			} else {
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						Integer value = matrixInteger.getMatrix()[k * sizeX * sizeY + j * sizeX + i];
						int r, g, b;
						g = (value >> 8) & 255;
						b = (value & 255);
						r = (value >> 16) & 255;
						RGB rgb = new RGB(r, g, b);
						imageData.setPixel(i, sizeY - j - 1, palette.getPixel(rgb));
					}
				}
			}
		}
		
		if ( matrix instanceof MatrixULong ) {
			MatrixULong matrixULong = (MatrixULong) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(sizeX, sizeY, 32, palette);
            for (int i = 0; i< sizeX; i++) {
				for (int j = 0; j< sizeY; j++) {
					long value = matrixULong.getMatrix()[k* sizeX * sizeY + j* sizeX + i];
					float h,s,b; 
					h = 0.0f; // Hue
					s = 1.0f; // Saturation
					b = 0.0f; // Brightness (min = 100)
					
					// first int value used to Brightness
					int fi = (int) value;
					b = fi/10000.0f; 
					if(b < 0.32f) b = 0.3f; 
					if(b > 1.0f) b = 1.0f;
					
					// second int value used to Hue
					int si = (int) (value >> 32);
					h = si/10000.0f; 
					if(h < 0.32f) h = 0.3f; 
					if(h > 1.0f) h = 1.0f;
					RGB rgb = new RGB(h*360.0f, s, b);
					imageData.setPixel(i, sizeY -j-1, palette.getPixel(rgb));
				}
			}
		}
		
		if ( matrix instanceof MatrixFloat ) {
			MatrixFloat matrixFloat = (MatrixFloat) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(sizeX, sizeY, 32, palette);
			for (int i = 0; i< sizeX; i++) {
				for (int j = 0; j< sizeY; j++) {
					Float value = matrixFloat.getMatrix()[k* sizeX * sizeY + j* sizeX + i];
					float h,s,b; 
					h = 0.0f; // Hue
					s = 1.0f; // Saturation
					b = 0.0f; // Brightness (min = 100)
					
					// Int value used to Brightness
					int pe = (int) (Math.floor(value.floatValue()));
					b = pe/10000.0f; 
					if(b < 0.32f) b = 0.3f; 
					if(b > 1.0f) b = 1.0f;
					
					// Float value used to Hue
					h = value.floatValue() - pe;
					if(value == 0.0f) { h=0.0f; s=0.0f; b=0.0f;}
					RGB rgb = new RGB(h*360.0f, s, b);
					imageData.setPixel(i, sizeY -j-1, palette.getPixel(rgb));
				}
			}
		}

		if ( imageData != null ){
			Image image = new Image(gc.getDevice(), imageData);
			Rectangle rect = gc.getClipping();
			gc.drawImage(image, 0, 0, sizeX, sizeY, 0, 0, rect.width, rect.height);
			image.dispose();
		}
	}
        
        @Override
        public void render3D(GC gc, RendererContext context, Matrix matrix, float angleY, float dRecul, float dFocal) {
            ImageData imageData = null;
            if ( matrix instanceof MatrixInteger ) {
                MatrixInteger matrixInteger = (MatrixInteger) matrix;
                PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);

            int SX = matrix.safeGetSizeXValue();
            int SY = matrix.safeGetSizeYValue();
            int SZ = matrix.safeGetSizeZValue();
           
            int RetineSX = 256;
            int RetineSY = 128;

            imageData = new ImageData(RetineSX, RetineSY, 32, palette);

            // Parametres de Camera
            // ************************
            float profondeur = 1.0f*(dRecul + Math.max(Math.max(SX,SY), SZ)); // Pour tout voir, doit etre assez grand pour englober dFocal+dRecul+max(SX,SY,SZ)

            for(int x = 0; x<RetineSX; x++) {
                for(int y = 0; y<RetineSY; y++) {

                    // Position in matrix
                    //int p = x + y*SX;

                    // Soit P le point de la retine en p (x,y)
                    // Dans le repere de l'univers, P est a la coordonnee p0(x0,y0,z0)

                    //           **************************** // Points de depart du ray-cast (point vise)
                    //
                    //                   010011220001           // Matrice 3D a visualiser dans la retine
                    //                   112200X10012           // X est le centre de la Matrice 3D a visualiser
                    //                   001001222511                    ^
                    //                                                   |
                    //						      dRecul
                    //                                                   v
                    //                   ***********            // Retine : Points d'arrivee du ray-cast
                    //                                                   ^
                    //                                                dFocal
                    //                                                   v
                    //                        *                 // Point de focus

                    // Centre de la matrice a visualiser
                    // ***********************
                    float xCenter = SX/2.0f;
                    float yCenter = SY/2.0f;
                    float zCenter = SZ/2.0f;

                    // Translation de la retine au centre de la matrice 3D a visualiser
                    // ************************
                    float x0 =  (float)x + xCenter - RetineSX/2.0f ;
                    float y0 =  (float)y + yCenter - RetineSY/2.0f ; 
                    float z0 =  zCenter ;
                    // **** FIN translation retine

                    // Rotation de P0 avec l'angleY de centre Center et autour de Y
                    // ****************
                    float xx0  = (x0-xCenter)*((float)Math.cos(angleY)) - (z0-zCenter)*((float)Math.sin(angleY)) + xCenter;
                    float yy0  =  y0;
                    float zz0  = (x0-xCenter)*((float)Math.sin(angleY)) + (z0-zCenter)*((float)Math.cos(angleY)) + zCenter;
                    // "Recul" (z positifs) de P selon le vecteur unitaire d'angleY
                    x0 = xx0 + dRecul * ((float)Math.cos(angleY + Math.PI/2.0f));
                    // Restera y0 a changer selon mouseY
                    z0 = zz0 + dRecul * ((float)Math.sin(angleY + Math.PI/2.0f )) ;
                    // **** FIN rotation P0

                    // Point de focus (derriere la retine => z encore plus "positif" que la retine)
                    // ***************
                    float xFocus = xCenter + (dRecul + dFocal) * ((float)Math.cos(angleY + Math.PI/2.0f));
                    float yFocus = yCenter ; 
                    float zFocus = zCenter + (dRecul + dFocal) * ((float)Math.sin(angleY + Math.PI/2.0f));
                    // **** FIN Point de Focus

                    // Vecteur de Direction du Focus vers le Depart
                    // *********************
                    float dx = x0 - xFocus ;
                    float dy = y0 - yFocus ;
                    float dz = z0 - zFocus ;
                    float L = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
                    // Vecteur direction unitaire
                    dx = dx / L; dy = dy / L; dz = dz / L;
                    // **** FIN Vecteur de Direction

                    // Point depart : c'est le point de depart (celui vise derriere la matrice 3D) du ray-cast qui va traverser la matrice 3D vers le point de focus et qui s'arrete a la retine
                    // *************
                    float xDepart = x0 +  profondeur*dx;
                    float yDepart = y0 +  profondeur*dy;
                    float zDepart = z0 +  profondeur*dz;
                    // **** FIN Point de Depart

                    // Calcul du Ray-Cast
                    // ********************
                    // Changement de signe du vecteur de direction pour pointer vers la Retine (donc aussi le point de Focus)
                    dx = -dx; dy = -dy; dz = -dz;

                    int ii0 = (int)xDepart, jj0 = (int)yDepart, kk0 = (int)zDepart;
                    int ii1 = (int)x0, jj1 = (int)y0, kk1 = (int)z0;
                    int ddi = Math.abs(ii1-ii0), ssi = ii0 < ii1 ? 1 : -1;
                    int ddj = Math.abs(jj1-jj0), ssj = jj0 < jj1 ? 1 : -1;
                    int ddk = Math.abs(kk1-kk0), ssk = kk0 < kk1 ? 1 : -1;
                    int ddm = ddi > ddj && ddi > ddk ? ddi : ddj > ddk ? ddj : ddk;
                    ii1 = ddm/2; jj1 = ddm/2; kk1 = ddm/2; /* error offset */

                    // Couleur de fond
                    float Rf = 0.0f; // Red final (initialise a la couleur de fond)
                    float Gf = 0.0f; // Green final (initialise a la couleur de fond)
                    float Bf = 0.0f; // Blue final (initialise a la couleur de fond)

                    if( (int)xDepart < 0) Rf = 1.0f;
                    if( (int)yDepart < 0) Gf = 1.0f;
                    //if( (int)zDepart < 0) Bf = 1.0f;
                    
                    float R=0.0f, G=0.0f, B=0.0f, A=0.0f;
                    boolean surface = false;
                    for(float pos = 0.0f; pos < profondeur; pos ++ ) {
                                    if( ii0>=0 && jj0 >= 0 && kk0 >= 0 && ii0 < SX && jj0 < SY && kk0 < SZ) {
                                            int pix3D = (matrixInteger.getMatrix()[kk0 * SX * SY + jj0 * SX + ii0]);
                                            R = ((float) (  (pix3D     )  & 0xFF)  ) / 256.0f  ;
                                            G = ((float) (  (pix3D >> 8)  & 0xFF)  ) / 256.0f  ;
                                            B = ((float) (  (pix3D >> 16) & 0xFF)  ) / 256.0f ;
                                            A = ((float) (  (pix3D >> 24) & 0xFF)  ) / 256.0f ; // OPACITE
                                            Rf = Rf * (1.0f - A) + R*A;       // Si A vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            Gf = Gf * (1.0f - A) + G*A;       // Si A vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            Bf = Bf * (1.0f - A) + B*A;       // Si A vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            surface = true;
                                    } else {
                                            if (  (ii0 == -1 && jj0 == -1 && kk0 >= 0 && kk0 < SZ)   ||  (ii0 == -1 && kk0 == -1 && jj0 >= 0 && jj0 < SY)   ||  (jj0 == -1 && kk0 == -1 && ii0 >= 0 && ii0 < SX)  ) { // Trace des axes x,y et z
                                                    Rf = 1.0f;       // Si opac vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                                    Gf = 1.0f;       // Si opac vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                                    Bf = 1.0f;       // Si opac vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            }
                                            if( Math.abs(ii0) % 10 == 0 && jj0 == -1) {Rf = 1.0f; Gf = 0.5f;}
                                            if( Math.abs(kk0) % 10 == 0 && jj0 == -1) {Rf = 1.0f; Gf = 0.5f;}
                                    }
                                    ii1 -= ddi; if (ii1 < 0) { ii1 += ddm; ii0 += ssi; }
                                    jj1 -= ddj; if (jj1 < 0) { jj1 += ddm; jj0 += ssj; }
                                    kk1 -= ddk; if (kk1 < 0) { kk1 += ddm; kk0 += ssk; }                                    
                    }

                    // **** FIN du Ray-Cast

                    // Reflet de la src lumineuse
                    float rt = 1.0f;
                    if(surface == true) rt = 1.0f-Math.abs(dx*dz);
                    
                    // Ecriture du pixel dans la retine
                    // *****************
                    RGB rgb = new RGB( (int)(255.0f*Rf*rt), (int)(255.0f*Gf*rt), (int)(255.0f*Bf*rt));       
                    imageData.setPixel(x, RetineSY-y-1, palette.getPixel(rgb));   
                    
                }
            }
            if ( imageData != null ){
                Image image = new Image(gc.getDevice(), imageData);
                Rectangle rect = gc.getClipping();
                gc.drawImage(image, 0, 0, RetineSX, RetineSY, 0, 0, rect.width, rect.height);
                image.dispose();
            }            
        }
            
        

    }        
        
        public void render3D_old(GC gc, RendererContext context, Matrix matrix, float angleY, float dRecul, float dFocal) {
            ImageData imageData = null;
            if ( matrix instanceof MatrixInteger ) {
                MatrixInteger matrixInteger = (MatrixInteger) matrix;
                PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);

            int SX = matrix.safeGetSizeXValue();
            int SY = matrix.safeGetSizeYValue();
            int SZ = matrix.safeGetSizeZValue();
           
            int RetineSX = 512;//SX;
            int RetineSY = 256;//SY;

            imageData = new ImageData(RetineSX, RetineSY, 32, palette);

            // Parametres de Camera
            // ************************
            //float dFocal = 100.0f ;
            //float dRecul = SX; // SX est le recul pr defaut : a changer par un attribut;
            float profondeur = dRecul + Math.max(Math.max(SX,SY), SZ); // Pour tout voir, doit etre assez grand pour englober dFocal+dRecul+max(SX,SY,SZ)
            float grain = profondeur / 100.0f; // Plus grain est proche de zero, plus le rendu sera precis (mais aussi plus lent de maniere proportionnelle. Si grain = 0.1f l'execution sera 10x plus lent que grain = 1.0f)

            for(int x = 0; x<RetineSX; x++) {
                for(int y = 0; y<RetineSY; y++) {

                    // Position in matrix
                    //int p = x + y*SX;

                    // Soit P le point de la retine en p (x,y)
                    // Dans le repere de l'univers, P est a la coordonnee p0(x0,y0,z0)

                    //           **************************** // Points de depart du ray-cast (point vise)
                    //
                    //                   010011220001           // Matrice 3D a visualiser dans la retine
                    //                   112200X10012           // X est le centre de la Matrice 3D a visualiser
                    //                   001001222511                    ^
                    //                                                   |
                    //						      dRecul
                    //                                                   v
                    //                   ***********            // Retine : Points d'arrivee du ray-cast
                    //                                                   ^
                    //                                                dFocal
                    //                                                   v
                    //                        *                 // Point de focus



                    // Angle de vue
                    // **************
                    //int t = (int)(System.currentTimeMillis() % 10000);
                    //float angleY = (t*6.28f/10000.0f-SX/2.0f) / 1.0f  ; //11.0f correcpond a angleY : a changer par un attribut ;

                    // Centre de la matrice a visualiser
                    // ***********************
                    float xCenter = SX/2.0f;
                    float yCenter = SY/2.0f;
                    float zCenter = SZ/2.0f;

                    // Translation de la retine au centre de la matrice 3D a visualiser
                    // ************************
                    float x0 =  (float)x + xCenter - RetineSX/2.0f ;
                    float y0 =  (float)y + yCenter - RetineSY/2.0f ; 
                    float z0 =  zCenter ;
                    // **** FIN translation retine

                    // Rotation de P0 avec l'angleY de centre Center et autour de Y
                    // ****************
                    float xx0  = (x0-xCenter)*((float)Math.cos(angleY)) - (z0-zCenter)*((float)Math.sin(angleY)) + xCenter;
                    float yy0  =  y0;
                    float zz0  = (x0-xCenter)*((float)Math.sin(angleY)) + (z0-zCenter)*((float)Math.cos(angleY)) + zCenter;
                    // "Recul" (z positifs) de P selon le vecteur unitaire d'angleY
                    x0 = xx0 + dRecul * ((float)Math.cos(angleY + Math.PI/2.0f));
                    // Restera y0 a changer selon mouseY
                    z0 = zz0 + dRecul * ((float)Math.sin(angleY + Math.PI/2.0f )) ;
                    // **** FIN rotation P0

                    // Point de focus (derriere la retine => z encore plus "positif" que la retine)
                    // ***************
                    float xFocus = xCenter + (dRecul + dFocal) * ((float)Math.cos(angleY + Math.PI/2.0f));
                    float yFocus = yCenter ; 
                    float zFocus = zCenter + (dRecul + dFocal) * ((float)Math.sin(angleY + Math.PI/2.0f));
                    // **** FIN Point de Focus

                    // Vecteur de Direction du Focus vers le Depart
                    // *********************
                    float dx = x0 - xFocus ;
                    float dy = y0 - yFocus ;
                    float dz = z0 - zFocus ;
                    float L = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
                    // Vecteur direction unitaire
                    dx = dx / L; dy = dy / L; dz = dz / L;
                    // **** FIN Vecteur de Direction

                    // Point depart : c'est le point de depart (celui vise derriere la matrice 3D) du ray-cast qui va traverser la matrice 3D vers le point de focus et qui s'arrete a la retine
                    // *************
                    float xDepart = x0 +  profondeur*dx;
                    float yDepart = y0 +  profondeur*dy;
                    float zDepart = z0 +  profondeur*dz;
                    // **** FIN Point de Depart

                    // Calcul du Ray-Cast
                    // ********************
                    // Changement de signe du vecteur de direction pour pointer vers la Retine (donc aussi le point de Focus)
                    dx = -dx; dy = -dy; dz = -dz;

                    // Couleur de fond
                    float Rf = 0.0f; // Red final (initialise a la couleur de fond)
                    float Gf = 0.0f; // Green final (initialise a la couleur de fond)
                    float Bf = 0.0f; // Blue final (initialise a la couleur de fond)

                    if( (int)yDepart < 0) Rf = 1.0f;
                    if( (int)yDepart < 0) Gf = 1.0f;
                    //if( (int)zDepart < 0) Bf = 1.0f;
                    
                    int i_old=0, j_old=0, k_old=0;
                    float R=0.0f, G=0.0f, B=0.0f, A=0.0f;
                    boolean surface = false;
                    for(float pos = 0.0f; pos < profondeur; pos += grain ) {
                            float xx = (xDepart + dx*pos), yy = (yDepart + dy*pos), zz = (zDepart + dz*pos) ;
                            int i = (int) Math.floor( xx ), j = (int) Math.floor(yy) , k = (int) Math.floor(zz) ;
                            if( i != i_old || j != j_old || k!= k_old) {
                                    i_old = i; j_old = j; k_old = k;
                                    if( i>=0 && j >= 0 && k >= 0 && i < SX && j < SY && k < SZ) {
                                            int pix3D = (matrixInteger.getMatrix()[k * SX * SY + j * SX + i]);
                                            R = ((float) (  (pix3D     )  & 0xFF)  ) / 256.0f  ;
                                            G = ((float) (  (pix3D >> 8)  & 0xFF)  ) / 256.0f  ;
                                            B = ((float) (  (pix3D >> 16) & 0xFF)  ) / 256.0f ;
                                            A = ((float) (  (pix3D >> 24) & 0xFF)  ) / 256.0f ; // OPACITE
                                            Rf = Rf * (1.0f - A) + R*A;       // Si A vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            Gf = Gf * (1.0f - A) + G*A;       // Si A vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            Bf = Bf * (1.0f - A) + B*A;       // Si A vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            surface = true;
                                    } else {
                                            /*if (  (i == -1 && j == -1 && k>=0 && k<SZ)   ||  (i == -1 && k == -1 && j>=0 && j<SY)   ||  (j == -1 && k == -1 && i>=0 && i<SX)  ) { // Trace des axes x,y et z
                                                    Rf = 0.5f;       // Si opac vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                                    Gf = 0.5f;       // Si opac vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                                    Bf = 0.5f;       // Si opac vaut 1 => R gagne totalement ; si opac vaut 0 => Rf n'est pas affecte
                                            }*/
                                            if( Math.abs(i) % 10 == 0 && j == -1) {Rf = 1.0f; Gf = 0.5f;}
                                            if( Math.abs(k) % 10 == 0 && j == -1) {Rf = 1.0f; Gf = 0.5f;}
                                    }
                            }
                    }

                    // **** FIN du Ray-Cast

                    // Reflet de la src lumineuse
                    float rt = 1.0f;
                    if(surface == true) rt = 1.0f-Math.abs(dx*dz);
                    
                    // Ecriture du pixel dans la retine
                    // *****************
                    RGB rgb = new RGB( (int)(255.0f*Rf*rt), (int)(255.0f*Gf*rt), (int)(255.0f*Bf*rt));       
                    imageData.setPixel(x, RetineSY-y-1, palette.getPixel(rgb));   
                    
                }
            }
            if ( imageData != null ){
                Image image = new Image(gc.getDevice(), imageData);
                Rectangle rect = gc.getClipping();
                gc.drawImage(image, 0, 0, RetineSX, RetineSY, 0, 0, rect.width, rect.height);
                image.dispose();
            }            
        }
            
        

    }
        
        
        
}
