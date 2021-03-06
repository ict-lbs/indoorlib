/*******************************************************************************
 * Copyright 2013 CRS4
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.crs4.roodin.moduletester;



import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import com.crs4.roodin.moduletester.R;
import com.crs4.roodin.moduletester.MapScrollView.OnDrawListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;



/**
 * 
 *  * 		drawRect(NW.x, NW.y, SE.x, SE.y) 	
 * 						X ---->    			
		|-----------------------------------| 	
		|			|			|			|
		|			|			|			|
	Y	|			|			|			|
		|___________NW			|			|
	|	|						|			|
	|	|						|			|
	|	|_______________________SE			|
	|	|									|
		|									|
		|									|
		|									|
		|									|
		|-----------------------------------|
 *  
 * @author ICT/LBS Team - CRS4 Sardinia, Italy
 *
 */
public class CustomView extends View implements OnDrawListener{
	Bitmap mBmp;
    Random mRnd;
    Paint mPaint;
    
    int w,h,bw,bh;
    int px=-1,py=-1;

	private Bitmap blockImg;
	private float translateX;
	private float translateY;
	private Block block;
	private float currentRotation;
	

	private ArrayList<Box> boxList;
	private float cellDimension;
	
    /**
     * @param context
     */
    public CustomView(Context context) {
            super(context);
            blockImg = BitmapFactory.decodeResource(context.getResources(), R.drawable.block);      //carichiamo l'immagine in una bitmap

            mBmp=BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_132);      //carichiamo l'immagine in una bitmap
            bw=mBmp.getWidth(); //larghezza bitmap
            bh=mBmp.getHeight();//altezza   
            mPaint=new Paint(); // pennello
            mPaint.setColor(Color.CYAN);     
            mPaint.setAntiAlias(true); 
            mRnd=new Random();
            
    }
    
    /**
     * @param bl
     */
    public void setBoxList(ArrayList<Box> bl){
    	this.boxList = bl;
    }
    
    
	/* (non-Javadoc)
	 * @see com.crs4.roodin.moduletester.MapScrollView.OnDrawListener#onDraw(android.graphics.Canvas, android.graphics.Matrix)
	 */
	@Override
	public void onDraw(Canvas canvas, Matrix matrix) {
		

		canvas.rotate(currentRotation, translateX, translateY);		
		canvas.drawBitmap(blockImg, 0, 0, null);
		drawBarreds().draw(canvas);
		drawRadar(canvas);
		

		canvas.restore();
	}
 
	/**
	 * @param canvas
	 */
	private void drawRadar(Canvas canvas){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		paint.setColor(Color.RED);		
		paint.setAlpha(100);

		float rotation = currentRotation; //gRotationZ_deg + initialMapRotation;
		
			for (int i = -35; i < 35; i = i + 2) {
				float arrowX1 = (float) (translateX - Math.sin(Math.toRadians(rotation  + i)) * 45);
				float arrowY1 = (float) (translateY - Math.cos(Math.toRadians(rotation + i)) * 45);
				canvas.drawLine(translateX, translateY, arrowX1, arrowY1, paint);
				
			}
		
		
		paint.setAlpha(100);
		paint.setColor(Color.RED);
		canvas.drawCircle(translateX, translateY, 7, paint);
		
		paint.setColor(Color.YELLOW);
		canvas.drawCircle(translateX, translateY, 6, paint);
		
		paint.setColor(Color.RED);
		canvas.drawCircle(translateX, translateY, 1, paint);
	}
    
	/**
	 * @return
	 */
	private Picture drawBarreds() {
		Picture pictureContent = new Picture();
		Canvas canvasContent = pictureContent.beginRecording(getWidth(), getHeight()); 
		Paint paintContent = new Paint(Paint.ANTI_ALIAS_FLAG);
		canvasContent.save();
		
		paintContent.setColor(Color.BLUE);
		paintContent.setStyle(Style.STROKE);
		paintContent.setStrokeWidth(2);
		paintContent.setAlpha(50);

		try {
			cellDimension = block.getCellDimension();
			JSONArray barred = block.getBarred();

			int rows = block.getShape().getInt(0);
			int cols = block.getShape().getInt(1);

			// this code draws all the shape:
			for (int i = 0; i < rows; i++) {               
				for (int j = 0; j < cols; j++) {               
					
					canvasContent.drawRect(cellDimension * j, 
									   		cellDimension * i, 
									   		cellDimension * j + cellDimension, 
									   		cellDimension * i + cellDimension, 
									   		paintContent);
				}
			}			
			paintContent.setColor(Color.RED);
			paintContent.setStyle(Style.FILL);
			paintContent.setAlpha(50);

			
			for (int i = 0; i < barred.length(); i++) {               
				JSONArray pos = barred.getJSONArray(i);
				canvasContent.drawRect(cellDimension * pos.getInt(1), 
									   cellDimension * pos.getInt(0), 
									   cellDimension * pos.getInt(1) + cellDimension, 
									   cellDimension * pos.getInt(0) + cellDimension, 
									   paintContent);
			}
			
			this.paintBoxList(canvasContent, paintContent);
			

		
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("Exception in DrawComponents.drawBarreds",""+e.getMessage());
		}

		canvasContent.restore();		
		pictureContent.endRecording();
		return pictureContent;

	}
	
	
	
    
	/**
	 * @param canvasContent
	 * @param paintContent
	 */
	private void paintBoxList(Canvas canvasContent, Paint paintContent) {
		for(Box b : boxList){
			paintContent.setColor(Color.GREEN);
			paintContent.setStyle(Style.FILL);
			paintContent.setAlpha(50);

			for (int i = b.getNorthWest().getX(); i < b.getSouthEast().getX(); i++) {               
				for (int j = b.getNorthWest().getY(); j < b.getSouthEast().getY(); j++) {   
					canvasContent.drawRect(cellDimension * i, 
									   cellDimension * j, 
									   cellDimension * i + cellDimension, 
									   cellDimension * j + cellDimension, 
									   paintContent);
				}
			}
		}
		
	}

	/**
	 * @param block
	 */
	public void setBlock(Block block) {
		this.block = block;
	}

    
	/**
	 * @param rotation
	 */
	public void setMapRotation(float rotation){
		currentRotation = rotation;
		invalidate();
	}


	/**
	 * @param posX
	 * @param posY
	 */
	public void setPosition(float posX, float posY) {		
		translateX = posX;
		translateY = posY;
	}




}
