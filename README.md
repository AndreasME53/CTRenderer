
Graphics java project designed to take in ct scans and render imagers, 
one can view the normal ct scans, full volume render scans and full gradient shading of the skulls from 3 angles,

Imagers of the program:

Starting window:

![image](https://user-images.githubusercontent.com/56043339/110861926-be1a8580-82b6-11eb-9bf3-ee7d0faafdb8.png)

Imagers of all three skull angles from the ct scan slices:

![image](https://user-images.githubusercontent.com/56043339/110862063-eace9d00-82b6-11eb-8171-1a51acf62232.png)

Imagers of all three skull angles from the ct scans to show volume rendering:

![image](https://user-images.githubusercontent.com/56043339/110862090-f3bf6e80-82b6-11eb-9d62-9ff334f55806.png)


This is using a simple ray caster and thus can visuallise the entire skull from all three angles 
the ray caster has been changed to identify parts of the ct scans that are bone and skin therefore truely bringing out better imagers using the entire ct data
there is a opacity slider to change the opacity 


A imager of the skull from the ct scans to show gradient shading:

![image](https://user-images.githubusercontent.com/56043339/110862767-a7c0f980-82b7-11eb-9c48-87e99288b904.png)
This graident shader finds the graident from the pixel data and determines the shading towards a light source, the light source can be moved along the x-axis


Improvemets on code like making it possible to see gradient shading from all three angles aswell as use interpolationto improve quality

final note the CT file of raw data cant be given as it doesnt belong to me
