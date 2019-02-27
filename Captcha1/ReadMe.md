### Verification Code Recognition Project First Edition: Captcha1

This project uses Tesseract V3.01 version (V3.02 version has been modified during training, multiple shapeclustering process)

**Tesseract usage: **
* Configure the environment variable TESSDATA_PREFIX = "D:\Tesseract-ocr\", which is the directory of tessdata. In the source code, you will find the corresponding font file for identification in this path.
* Command format:
`tesseract imagename outputbase [-l lang] [-psm pagesegmode] [configfile...]`
* Only recognized as numbers
`tesseract imagename outputbase -l eng digits`
* Solve the empty page!!
**-psm N**

7 = Treat the image as a single text line
Tesseract imagename outputbase -l eng -psm 7
* The configfile parameter values ​​are the file names in the tessdata\configs and tessdata\tessconfigs directories:
`tesseract imagename outputbase -l eng nobatch`


**Verification code identification item usage method 1:**
 
* Put the downloaded image in the ./pic directory.

Captcha image name: get_random.jpg
Price image name: get_price_img.png

* Command format:

Captcha image recognition: python tess_test.py ./pic/get_random.jpg
Price image recognition: python tess_test.py ./pic/get_price_img.png
  
Print out the results of the recognition

To save the result in the temporary text file **temp.txt**, modify the code "**cleanup_scratch_flag = True**" in pytessr_pro.py to "**cleanup_scratch_flag = False**"
Send feedback
History
Saved
Community
