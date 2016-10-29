# WHAITE
# Samuel
# 160656949
.data
displayBuffer:  .space 0x80000  # space for 512x256 bitmap display 
errorBuffer:    .space 0x80000  # space to store match function
templateBuffer: .space 0x400	# space for 8x8 template
imageFileName:    .asciiz "pxlcon512x256cropgs.raw"  # filename of image to load 
templateFileName: .asciiz "template8x8gs.raw"	     # filename of template to load
# struct bufferInfo { int *buffer, int width, int height, char* filename }
imageBufferInfo:    .word displayBuffer  512 16  imageFileName
errorBufferInfo:    .word errorBuffer    512 16  0
templateBufferInfo: .word templateBuffer 8   8    templateFileName

.text
main:	la $a0, imageBufferInfo
	jal loadImage
	la $a0, templateBufferInfo
	jal loadImage
	la $a0, imageBufferInfo
	la $a1, templateBufferInfo
	la $a2, errorBufferInfo
	jal matchTemplate        # MATCHING DONE HERE
	la $a0, errorBufferInfo
	jal findBest
	la $a0, imageBufferInfo
	move $a1, $v0
	jal highlight
	la $a0, errorBufferInfo	
	jal processError
	li $v0, 10		# exit
	syscall
	

##########################################################
# matchTemplate( bufferInfo imageBufferInfo, bufferInfo templateBufferInfo, bufferInfo errorBufferInfo )
# NOTE: struct bufferInfo { int *buffer, int width, int height, char* filename }
matchTemplate:	addi $sp, $sp, -36 #my original idea had some obscure bug so instead of that enjoy the "cache friendly implementation" outlined in the assignment pdf.
		sw $s0, 0($sp)	   #no changes where made to the idea, this code is a literal translation of the java code
		sw $s1, 4($sp)
		sw $s2, 8($sp)
		sw $s3, 12($sp)
		sw $s4, 16($sp)
		sw $s5, 20($sp)
		sw $s6, 24($sp)
		sw $s7, 28($sp)
		
		add $t1, $0, $0
		sw $t1, 32($sp)
bigLoop:	lw $t1, 32($sp)
		lw $t0, 0($a1)
		add $t0, $t0, $t1
		lw $t2, 0($t0) #load template row into registers $t2-$t9
		lw $t3, 0x4($t0)
		lw $t4, 0x8($t0)
		lw $t5, 0xc($t0)
		lw $t6, 0x10($t0)
		lw $t7, 0x14($t0)
		lw $t8, 0x18($t0)
		lw $t9, 0x1c($t0)
		
		srl $t2, $t2, 16 #get only the good parst
		srl $t3, $t3, 16
		srl $t4, $t4, 16
		srl $t5, $t5, 16
		srl $t6, $t6, 16
		srl $t7, $t7, 16
		srl $t8, $t8, 16
		srl $t9, $t9, 16		
		
		lw $s6, 32($sp)
		srl $s6, $s6, 3
		lw $s7, 4($a0)
		mult $s6, $s7
		mflo $s6
		add $t1, $0, $0 #bigYloop counter. counts row offset
		add $t0, $0, $0 #bigYloop counter. counts number of rows
		lw $s1, 0($a0) #$s1 contains address of image buffer	
		lw $s0, 0($a2) #s0 contains address of error buffer	
		add $s1, $s1, $s6
bigYLoop:	
		add $s3, $s1, $t1 #start postition of row in image buffer
		add $s4, $s0, $t1 #start position of row in error buffer
		add $s5, $0, $0 #bigXloop counter
bigXLoop:	
		
		
		lw $s7, 0($s4)	#load existing value	
		
		lw $s6, 0($s3) #find differences in the values
		srl $s6, $s6, 16
		sub $v0, $s6, $t2
		slt $v1, $v0, $0
		beq $v1, $0, skip1
		sub $v0, $0, $v0
skip1:		add $s7, $s7, $v0
		
		lw $s6, 4($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t3
		slt $v1, $v0, $0
		beq $v1, $0, skip2
		sub $v0, $0, $v0
skip2:		add $s7, $s7, $v0
		
		lw $s6, 8($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t4
		slt $v1, $v0, $0
		beq $v1, $0, skip3
		sub $v0, $0, $v0
skip3:		add $s7, $s7, $v0
		
		lw $s6, 12($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t5
		slt $v1, $v0, $0
		beq $v1, $0, skip4
		sub $v0, $0, $v0
skip4:		add $s7, $s7, $v0
		
		lw $s6, 16($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t6
		slt $v1, $v0, $0
		beq $v1, $0, skip5
		sub $v0, $0, $v0
skip5:		add $s7, $s7, $v0
		
		lw $s6, 20($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t7
		slt $v1, $v0, $0
		beq $v1, $0, skip6
		sub $v0, $0, $v0
skip6:		add $s7, $s7, $v0
		
		lw $s6, 24($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t8
		slt $v1, $v0, $0
		beq $v1, $0, skip7
		sub $v0, $0, $v0
skip7:		add $s7, $s7, $v0
		
		lw $s6, 28($s3)
		srl $s6, $s6, 16
		sub $v0, $s6, $t9
		slt $v1, $v0, $0
		beq $v1, $0, skip8
		sub $v0, $0, $v0
skip8:		add $s7, $s7, $v0
		
		sw $s7, 0($s4) #save the result
		
		addi $s3, $s3, 4 #increase start position in image buffer
		addi $s4, $s4, 4 #increase writing position in error buffer
#-------------------------------------------------# big X loop conditionals		
		addi $s5, $s5, 4
		lw  $s6, 4($a0)
		sll $s6, $s6, 2
		addi $s6, $s6, -28
		bne $s5, $s6, bigXLoop
#--------------------------------------------------# end of big X loop	
#--------------------------------------------------# big Y loop conditionals
		lw $s2, 4($a0)
		lw $s3, 8($a0)
		addi $s3, $s3, -7
		sll $s2, $s2, 2	
		add $t1, $t1, $s2
		add $t0, $t0, 1
		bne $t0, $s3, bigYLoop
#--------------------------------------------------# end of big Y loop
#--------------------------------------------------# big loop conditionals		
		lw $t1, 32($sp)
		addi $t1, $t1, 0x20
		sw $t1, 32($sp)
		bne $t1, 0x100, bigLoop
#--------------------------------------------------# end of overall loop
		#time to restore the stack				
		lw $s0, 0($sp)
		lw $s1, 4($sp)
		lw $s2, 8($sp)
		lw $s3, 12($sp)
		lw $s4, 16($sp)
		lw $s5, 20($sp)
		lw $s6, 24($sp) #restoring s registers
		lw $s7, 28($sp)
		addi $sp, $sp, 36
		jr $ra	
	
	
	
	
###############################################################
# loadImage( bufferInfo* imageBufferInfo )
# NOTE: struct bufferInfo { int *buffer, int width, int height, char* filename }
loadImage:	lw $a3, 0($a0)  # int* buffer
		lw $a1, 4($a0)  # int width
		lw $a2, 8($a0)  # int height
		lw $a0, 12($a0) # char* filename
		mul $t0, $a1, $a2 # words to read (width x height) in a2
		sll $t0, $t0, 2	  # multiply by 4 to get bytes to read
		li $a1, 0     # flags (0: read, 1: write)
		li $a2, 0     # mode (unused)
		li $v0, 13    # open file, $a0 is null-terminated string of file name
		syscall
		move $a0, $v0     # file descriptor (negative if error) as argument for read
  		move $a1, $a3     # address of buffer to which to write
		move $a2, $t0	  # number of bytes to read
		li  $v0, 14       # system call for read from file
		syscall           # read from file
        	# $v0 contains number of characters read (0 if end-of-file, negative if error).
        	# We'll assume that we do not need to be checking for errors!
		# Note, the bitmap display doesn't update properly on load, 
		# so let's go touch each memory address to refresh it!
		move $t0, $a3	   # start address
		add $t1, $a3, $a2  # end address
loadloop:	lw $t2, ($t0)
		sw $t2, ($t0)
		addi $t0, $t0, 4
		bne $t0, $t1, loadloop
		jr $ra
		
		
#####################################################
# (offset, score) = findBest( bufferInfo errorBuffer )
# Returns the address offset and score of the best match in the error Buffer
findBest:	lw $t0, 0($a0)     # load error buffer start address	
		lw $t2, 4($a0)	   # load width
		lw $t3, 8($a0)	   # load height
		addi $t3, $t3, -7  # height less 8 template lines minus one
		mul $t1, $t2, $t3
		sll $t1, $t1, 2    # error buffer size in bytes	
		add $t1, $t0, $t1  # error buffer end address
		li $v0, 0		# address of best match	
		li $v1, 0xffffffff 	# score of best match	
		lw $a1, 4($a0)    # load width
        	addi $a1, $a1, -7 # initialize column count to 7 less than width to account for template
fbLoop:		lw $t9, 0($t0)        # score
		sltu $t8, $t9, $v1    # better than best so far?
		beq $t8, $zero, notBest
		move $v0, $t0
		move $v1, $t9
notBest:	addi $a1, $a1, -1
		bne $a1, $0, fbNotEOL # Need to skip 8 pixels at the end of each line
		lw $a1, 4($a0)        # load width
        	addi $a1, $a1, -7     # column count for next line is 7 less than width
        	addi $t0, $t0, 28     # skip pointer to end of line (7 pixels x 4 bytes)
fbNotEOL:	add $t0, $t0, 4
		bne $t0, $t1, fbLoop
		lw $t0, 0($a0)     # load error buffer start address	
		sub $v0, $v0, $t0  # return the offset rather than the address
		jr $ra
		

#####################################################
# highlight( bufferInfo imageBuffer, int offset )
# Applies green mask on all pixels in an 8x8 region
# starting at the provided addr.
highlight:	lw $t0, 0($a0)     # load image buffer start address
		add $a1, $a1, $t0  # add start address to offset
		lw $t0, 4($a0) 	# width
		sll $t0, $t0, 2	
		li $a2, 0xff00 	# highlight green
		li $t9, 8	# loop over rows
highlightLoop:	lw $t3, 0($a1)		# inner loop completely unrolled	
		and $t3, $t3, $a2
		sw $t3, 0($a1)
		lw $t3, 4($a1)
		and $t3, $t3, $a2
		sw $t3, 4($a1)
		lw $t3, 8($a1)
		and $t3, $t3, $a2
		sw $t3, 8($a1)
		lw $t3, 12($a1)
		and $t3, $t3, $a2
		sw $t3, 12($a1)
		lw $t3, 16($a1)
		and $t3, $t3, $a2
		sw $t3, 16($a1)
		lw $t3, 20($a1)
		and $t3, $t3, $a2
		sw $t3, 20($a1)
		lw $t3, 24($a1)
		and $t3, $t3, $a2
		sw $t3, 24($a1)
		lw $t3, 28($a1)
		and $t3, $t3, $a2
		sw $t3, 28($a1)
		add $a1, $a1, $t0	# increment address to next row	
		add $t9, $t9, -1	# decrement row count
		bne $t9, $zero, highlightLoop
		jr $ra

######################################################
# processError( bufferInfo error )
# Remaps scores in the entire error buffer. The best score, zero, 
# will be bright green (0xff), and errors bigger than 0x4000 will
# be black.  This is done by shifting the error by 5 bits, clamping
# anything bigger than 0xff and then subtracting this from 0xff.
processError:	lw $t0, 0($a0)     # load error buffer start address
		lw $t2, 4($a0)	   # load width
		lw $t3, 8($a0)	   # load height
		addi $t3, $t3, -7  # height less 8 template lines minus one
		mul $t1, $t2, $t3
		sll $t1, $t1, 2    # error buffer size in bytes	
		add $t1, $t0, $t1  # error buffer end address
		lw $a1, 4($a0)     # load width as column counter
        	addi $a1, $a1, -7  # initialize column count to 7 less than width to account for template
pebLoop:	lw $v0, 0($t0)        # score
		srl $v0, $v0, 5       # reduce magnitude 
		slti $t2, $v0, 0x100  # clamp?
		bne  $t2, $zero, skipClamp
		li $v0, 0xff          # clamp!
skipClamp:	li $t2, 0xff	      # invert to make a score
		sub $v0, $t2, $v0
		sll $v0, $v0, 8       # shift it up into the green
		sw $v0, 0($t0)
		addi $a1, $a1, -1        # decrement column counter	
		bne $a1, $0, pebNotEOL   # Need to skip 8 pixels at the end of each line
		lw $a1, 4($a0)        # load width to reset column counter
        	addi $a1, $a1, -7     # column count for next line is 7 less than width
        	addi $t0, $t0, 28     # skip pointer to end of line (7 pixels x 4 bytes)
pebNotEOL:	add $t0, $t0, 4
		bne $t0, $t1, pebLoop
		jr $ra
