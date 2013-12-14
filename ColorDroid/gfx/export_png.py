#!/usr/bin/env python

import os
import subprocess
import sys

INKSCAPE_PATH = '/Applications/Inkscape.app/Contents/Resources/bin/inkscape'
EXPORTED_DPIS = (
	(90, 'mdpi'),
	(135, 'hdpi'),
	(180, 'xhdpi'),
	(270, 'xxhdpi')
)

def export_files(res_path, filenames):
	process = subprocess.Popen([INKSCAPE_PATH, '--without-gui', '--shell'], stdin=subprocess.PIPE)
	try:
		for filename in filenames:
			for dpi, dpi_name in EXPORTED_DPIS:
				png_filename = os.path.splitext(os.path.basename(filename))[0] + '.png'
				png_dir = os.path.join(res_path, 'res/drawable-' + dpi_name)
				if not os.path.exists(png_dir):
					print >> sys.stderr, '[Error] path does not exist:', png_dir
					return -1
				png_filepath = os.path.join(png_dir, png_filename) 
				process.stdin.write(
					'--file=%s --export-area-page --export-png=%s --export-dpi=%s\n'
					% (filename, png_filepath, dpi))
	finally:
		process.stdin.close()
		return_code = process.wait()
		print ''
	return return_code

if __name__=='__main__':
	sys.exit(export_files(sys.argv[1], sys.argv[2:]))
