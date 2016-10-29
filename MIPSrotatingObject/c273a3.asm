.data
.globl bitmapDisplay
bitmapDisplay: 	.space 0x80000
bitmapBuffer:	.space 0x80000
width: 		.word 512
height: 	.word 512
testMatrix: .float
	331.3682 156.83034 -163.18181 1700.7253
	-39.86386 -48.649902 -328.51334 1119.5535
	0.13962941 1.028447 -0.64546686 0.48553467
	0.11424224 0.84145665 -0.52810925 6.3950152
rotateMatrix: .float
	0.9994 0.0349 0 0
	-0.0349 0.9994 0 0
	0 0 1 0
	0 0 0 1

testVec1: .float 1 0 0 0
testVec2: .float 0 1 0 0
testVec3: .float 0 0 1 0
testVec4: .float 0 0 0 1
testResult: .space 16

.text
		addi $a0, $0, 0x00000000
		addi $sp, $sp, -4
		sw $ra, 0($sp)
		addi $a0, $0, 0x00808080
		jal clearBuffer
		jal drawTeapot
		jal copyBuffer
		jal rotateTeapot
		lw $ra, 0($sp)
		addi $sp, $sp, 4				
		addi $v0, $0, 10		
		syscall
		
clearBuffer:	add $t0, $0, $0
clearLoop:	sw $a0, 0x10090000($t0)
		sw $a0, 0x10090004($t0)
		sw $a0, 0x10090008($t0)
		sw $a0, 0x1009000c($t0)
		addi $t0, $t0, 0x10
		bne $t0, 0x80000, clearLoop
		jr $ra
		
copyBuffer:	add $t0, $0, $0
copyLoop: 	lw $t1, 0x10090000($t0)
		sw $t1, 0x10010000($t0)
		lw $t1, 0x10090004($t0)
		sw $t1, 0x10010004($t0)
		lw $t1, 0x10090008($t0)
		sw $t1, 0x10010008($t0)
		lw $t1, 0x1009000c($t0)
		sw $t1, 0x1001000c($t0)
		addi $t0, $t0, 0x10
		bne $t0, 0x80000, copyLoop
		jr $ra		

drawPoint: 	lw $t0, width
		slt $t1, $a0, $0
		bne $t1, $0, pointEnd
		slt $t1, $a1, $0
		bne $t1, $0, pointEnd
		slti $t1, $a0, 513
		beq $t1, $0, pointEnd
		slti $t1, $a1, 257
		beq $t1, $0, pointEnd
		mult $t0, $a1
		mflo $t0
		add $t0, $t0, $a0
		add $t0, $t0, $t0
		add $t0, $t0, $t0
		addi $t1, $0, 0x00fafafa
		sw $t1, 0x10090000($t0)
pointEnd:	jr $ra
		
drawLine: 	addi $sp, $sp, -12
		sw $ra, 8($sp)
		addi $t0, $0, 1  	#$t0 = offsetX
		addi $t1, $0, 1  	#$t1 = offsetY
		add $t2, $0, $a0	#$t2 = x
		add $t3, $0, $a1	#$t3 = y
		sub $t4, $a2, $a0	#$t4 = dX
		sub $t5, $a3, $a1	#$t5 = dY
		slt $t6, $t4, $0
		beq $t6, $0, firstPass
		sub $t4, $0, $t4
		addi $t0, $0, -1
firstPass:	slt $t6, $t5, $0
		beq $t6, $0, secondPass
		sub $t5, $0, $t5
		addi $t1, $0, -1
secondPass:	sw $t0, 4($sp)
		sw $t1, 0($sp)
	 	jal drawPoint		#a0 and a1 already set to the correct points
		lw $t1, 0($sp)
		lw $t0, 4($sp)
		slt $t6, $t5, $t4
		beq $t6, $0, firstElse
		add $t7, $0, $t4	#$t7 = error
		beq $t2, $a2, lineEnd	#we know a2 is not modified in drawPoint so its ok to use it without saving/restoring
firstWhile:	add $t6, $t5, $t5
		sub $t7, $t7, $t6
		slt $t6, $t7, $0
		beq $t6, $0, thirdPass
		add $t3, $t3, $t1
		add $t6, $t4, $t4
		add $t7, $t7, $t6
thirdPass:	add $t2, $t2, $t0
		add $a0, $0, $t2
		add $a1, $0, $t3
		sw $t0, 4($sp)
		sw $t1, 0($sp)
		jal drawPoint
		lw $t1, 0($sp)
		lw $t0, 4($sp)
		bne $t2, $a2, firstWhile
		j lineEnd
firstElse: 	add $t7, $0, $t5
		beq $t3, $a3, lineEnd #we know a3 is not modified in drawPoint so its ok to use it without saving/restoring
secondWhile:	add $t6, $t4, $t4
		sub $t7, $t7, $t6
		slt $t6, $t7, $0
		beq $t6, $0, fourthPass
		add $t2, $t2, $t0
		add $t6, $t5, $t5
		add $t7, $t7, $t6
fourthPass:	add $t3, $t3, $t1
		add $a0, $t2, $0
		add $a1, $t3, $0
		sw $t0, 4($sp)
		sw $t1, 0($sp)
		jal drawPoint
		lw $t1, 0($sp)
		lw $t0, 4($sp)
		bne $t3, $a3, secondWhile
lineEnd:	lw $ra 8($sp)
		addi $sp, $sp, 12
		jr $ra
mulMatrix:	lwc1 $f4, 0($a0) #a0: matrix
		lwc1 $f6, 0($a1) #a1: vector
		mul.s $f8, $f4, $f6
		lwc1 $f4, 4($a0)
		lwc1 $f6, 4($a1)
		mul.s $f10, $f4, $f6
		lwc1 $f4, 8($a0)
		lwc1 $f6, 8($a1)
		mul.s $f16, $f4, $f6
		lwc1 $f4, 12($a0)
		lwc1 $f6, 12($a1)
		mul.s $f18, $f4, $f6
		add.s $f8, $f8, $f10
		add.s $f8, $f8, $f16
		add.s $f8, $f8, $f18
		swc1 $f8, 0($a2) #a2: testResult
		lwc1 $f4, 16($a0)
		lwc1 $f6, 0($a1)
		mul.s $f8, $f4, $f6
		lwc1 $f4, 20($a0)
		lwc1 $f6, 4($a1)
		mul.s $f10, $f4, $f6
		lwc1 $f4, 24($a0)
		lwc1 $f6, 8($a1)
		mul.s $f16, $f4, $f6
		lwc1 $f4, 28($a0)
		lwc1 $f6, 12($a1)
		mul.s $f18, $f4, $f6
		add.s $f8, $f8, $f10
		add.s $f8, $f8, $f16
		add.s $f8, $f8, $f18
		swc1 $f8, 4($a2)
		lwc1 $f4, 32($a0)
		lwc1 $f6, 0($a1)
		mul.s $f8, $f4, $f6
		lwc1 $f4, 36($a0)
		lwc1 $f6, 4($a1)
		mul.s $f10, $f4, $f6
		lwc1 $f4, 40($a0)
		lwc1 $f6, 8($a1)
		mul.s $f16, $f4, $f6
		lwc1 $f4, 44($a0)
		lwc1 $f6, 12($a1)
		mul.s $f18, $f4, $f6
		add.s $f8, $f8, $f10
		add.s $f8, $f8, $f16
		add.s $f8, $f8, $f18
		swc1 $f8, 8($a2)
		lwc1 $f4, 48($a0)
		lwc1 $f6, 0($a1)
		mul.s $f8, $f4, $f6
		lwc1 $f4, 52($a0)
		lwc1 $f6, 4($a1)
		mul.s $f10, $f4, $f6
		lwc1 $f4, 56($a0)
		lwc1 $f6, 8($a1)
		mul.s $f16, $f4, $f6
		lwc1 $f4, 60($a0)
		lwc1 $f6, 12($a1)
		mul.s $f18, $f4, $f6
		add.s $f8, $f8, $f10
		add.s $f8, $f8, $f16
		add.s $f8, $f8, $f18
		swc1 $f8, 12($a2)
		jr $ra
drawTeapot:	addi $sp, $sp, -16
		sw $ra, 12($sp)
		add $t2, $0, $0		
teapotLoop:	la $a2, testResult #loop goes through every "line" of teapot data.
		la $t1, LineData
		la $a0, testMatrix
		add $a1, $t1, $t2
		sw $t2, 8($sp)
		jal mulMatrix 
		lwc1 $f4, testResult
		lwc1 $f6, testResult + 4
		lwc1 $f8, testResult + 12
		div.s $f4, $f4, $f8
		div.s $f6, $f6, $f8
		cvt.w.s $f4, $f4
		cvt.w.s $f6, $f6
		swc1 $f4, 4($sp) #save the first x and y components on the stack
		swc1 $f6, 0($sp)
		la $a0, testMatrix
		la $t1, LineData
		la $a2, testResult
		lw $t2, 8($sp)
		add $a1, $t1, $t2
		addi $a1, $a1, 0x10
		jal mulMatrix
		lwc1 $f4, testResult
		lwc1 $f6, testResult + 4
		lwc1 $f8, testResult + 12
		div.s $f4, $f4, $f8
		div.s $f6, $f6, $f8
		lw $a0, 4($sp) #load first x and y components into a0 and a1
		lw $a1, 0($sp)
		cvt.w.s $f4, $f4
		cvt.w.s $f6, $f6
		swc1 $f4, 4($sp) #use the stack to bring the bring the second x and y components into a2 and a3
		swc1 $f6, 0($sp)
		lw $a2, 4($sp)
		lw $a3, 0($sp)		
		jal drawLine	#draw the line between the two endpoints
		lw $t2, 8($sp)
		lw $t3, LineCount
		addi $t4, $0, 0x20
		mult $t3, $t4
		mflo $t3
		addi $t2, $t2, 0x20	
		bne $t2, $t3, teapotLoop	#loop!
		lw $ra, 12($sp)
		addi $sp, $sp, 16
		jr $ra
rotateTeapot:	addi $sp, $sp, -12
		sw $ra, 8($sp)
		add $t0, $0, $0
		sw $t0, 4($sp)		
rotateLoop1: 	li $a0, 0x00808080 #rotates and draws the teapot 30 times
		jal clearBuffer
		add $t1, $0, $0
		sw $t1, 0($sp)			
rotateLoop2:	la $a0, rotateMatrix #applies the rotation matrix to all the endpoint data
		la $a1, LineData
		add $a1, $a1, $t1
		la $a2, testResult
		jal mulMatrix #transform first endpoint
		lw $t1, 0($sp)
		lw $t2, testResult
		sw $t2, LineData($t1)
		lw $t2, testResult + 4
		sw $t2, LineData+4($t1)
		lw $t2, testResult + 8
		sw $t2, LineData+8($t1)
		lw $t2, testResult + 12
		sw $t2, LineData+12($t1)
		la $a0, rotateMatrix
		la $a1, LineData
		addi $a1, $a1, 0x10
		add $a1, $a1, $t1
		la $a2, testResult
		jal mulMatrix #transform second endpoint
		lw $t1, 0($sp)
		lw $t2, testResult
		sw $t2, LineData+16($t1)
		lw $t2, testResult + 4
		sw $t2, LineData+20($t1)
		lw $t2, testResult + 8
		sw $t2, LineData+24($t1)
		lw $t2, testResult + 12
		sw $t2, LineData+28($t1)
		addi $t1, $t1, 0x20
		sw $t1, 0($sp)
		lw $t3, LineCount
		addi $t4, $0, 0x20
		mult $t3, $t4
		mflo $t3
		bne $t1, $t3, rotateLoop2 #loop!
		jal drawTeapot
		jal copyBuffer
		lw $t0, 4($sp)
		addi $t0, $t0, 1
		sw $t0, 4($sp)
		bne $t0, 30, rotateLoop1 #loop!
		lw $ra, 8($sp)
		addi $sp, $sp, 12
		jr $ra








