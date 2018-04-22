uint x = get_global_id(0);
uint y = get_global_id(1);
uint z = get_global_id(2);

// Position in matrix
uint p = x + y*workSizeX + z*workSizeX*workSizeY;

// Work here
