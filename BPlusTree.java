import java.io.*;
import java.util.*;

public class BPlusTree{

	int m; //the order of tree
	int n; //minimum number of required children/pointers 

	//for testing/printing purposes only (NOT USED IN BPLUSTREE IMPLEMENTATION!!)
	public Queue<Node> q = new LinkedList<Node>();

	public class Node{

		public boolean isLeaf;
		public boolean isRoot;

		//number of keys
		public int numKeys;

		//keys
		public double[] keys = new double[m];//#keys can be at max m-1, but adding a pad element to do manipulations while insert
	

		//value node to handle duplicates
		public ValueNode[] values = new ValueNode[m];

		//children
		public Node[] pointers = new Node[m+1];//can be at max m, and adding a padding element

		//pointers for doubly linked list
		public Node next;
		public Node previous;

		//parent
		public Node parent;

		Node(){

		}


		Node(boolean isLeaf, boolean isRoot){
			this.isLeaf = isLeaf;
			this.isRoot = isRoot;
		}

	}

	Node root; 

	//initialise the tree and the root
	BPlusTree(int m){
		this.m = m;
		double d = m/2;
		n = (int)Math.floor(d);
		root = new Node(true,true);
	}

	//represents a value data structure, to aid in 
	//chaining of the values with same keys
	public class ValueNode{
		public String value;
		public ValueNode next;

		ValueNode(String value, ValueNode next){
			this.value = value;
			this.next = next;
		}

	}

	//performs the search operation on the tree based on a key
	//and returns a leaf node where, either the key exists
	//or the new key-value pair should be added
	public Node binSearch(double key, Node node){
		
		int i=0;
		for(i=0;i<node.numKeys;i++){

			if(key == node.keys[i]){

				//see if it is leaf, if yes then return else check the right pointers
				if(node.isLeaf)
					return node;
				else
					return binSearch(key,node.pointers[i+1]);
				
			}
			else if(key < node.keys[i]){
				
				if(node.pointers[i]==null && node.isLeaf) 
					return node;
				else 
					return binSearch(key,node.pointers[i]);

			}
			
			
		}
		//you've traversed the whole list and not found it or is not lesser than any
		//i would be equal to numKeys by now
		if(node.pointers[i]==null && node.isLeaf) return node;
		else return binSearch(key, node.pointers[i]);

	}

	//handles insertion of a new pair
	public void insertPair(double key, String value){
	//first search and reach the node to get either the node or where it should be inserted
	//BUT WHATEVER THE CASE YOU GET A LEAF NODE
		Node leaf = binSearch(key,root);
		

	//check if the returned node was a hit or a miss
		boolean isAlreadyPresent = false;
		int a;
		for( a=0;a<leaf.numKeys;a++){
			if(key == leaf.keys[a]){
				isAlreadyPresent = true; break;
			}
		}
		if(isAlreadyPresent){
			//System.out.println("already exists");
			//handling duplicate case
			ValueNode temp;
			temp = leaf.values[a];
			while(temp.next!=null){
				temp = temp.next;
			}
			temp.next = new ValueNode(value,null);

		}
		else{
		//not already present, so insert key and value pair

			//if not overflow
			//mergeIntoLeaf
			if(leaf.numKeys < m-1){
				
				mergeIntoLeaf(leaf,key,value);
			}
			else{

			//SPLITTING OF THE LEAF HANDLED HERE ITSELF AND THEN FED INTO MERGEINTOPARENT()	
			//else split
			//update current leaf and merge smallest key of the right IntoParent
				// make a new leaf
				mergeIntoLeaf(leaf,key,value);
				Node newLeaf = new Node(true,false);

				//populate new leaf with 2nd half data of previous leaf
				for(int i=n,j=0;i<m;i++,j++){
						
						newLeaf.keys[j] = leaf.keys[i];
						newLeaf.values[j] = leaf.values[i];

						//remove these very entries from the old leaf as well
						leaf.keys[i] = 0;
						leaf.values[i] = null;

						//handling number of keys
						leaf.numKeys--;
						newLeaf.numKeys++;
				}
 
				// take care of old leaf pointers and the new leaf pointers for the linked list
				newLeaf.next = leaf.next;
				newLeaf.previous = leaf;
				leaf.next = newLeaf;

				mergeIntoParent(leaf,newLeaf,newLeaf.keys[0]);

			}

			

			
		}	
		

	}

	//directly adds the new pair into correct spot in the leaf	
	public void mergeIntoLeaf(Node leaf,double key,String value){
	
		int i=0;

		for(i=0;i<leaf.numKeys;i++){
			if(key<leaf.keys[i])
				break;
		}
		//i is the index of where the key should be inserted
		int whereToInsert = i;

		for(i=leaf.numKeys;i>whereToInsert;i--){
			//first move everything one space ahead
			//and then insert the new key value pair
			//where it is supposed to be inserted
			leaf.keys[i] = leaf.keys[i-1];
			leaf.values[i] = leaf.values[i-1];


		}

		//The above loop emptied up the spot where we are supposed to insert
		//so now insert
		leaf.keys[whereToInsert] = key;
		leaf.values[whereToInsert] = new ValueNode(value,null);
		leaf.values[whereToInsert].next=null;

		leaf.numKeys++;
	}

	//used as a basic data structure in splitNonLeaf method
	public class SplitReturn{
		public Node left;
		public Node right;
		public double splitKey;

		SplitReturn(Node left, Node right, double splitKey){
			this.left = left;
			this.right = right;
			this.splitKey = splitKey;
		}
	}

	//handles splitting of non-leaf elements
	public SplitReturn splitNonLeaf(Node nonLeafNode,Node emptyNode){

		//because we came into splitting, this emplies that 
		//nonLeafNode has numKeys = m i.e. >than m-1
		double splitKey = nonLeafNode.keys[n];

		for(int i=n,j=0;i<m;i++,j++){
			
			emptyNode.keys[j] = nonLeafNode.keys[i];
			nonLeafNode.keys[i] = 0;
			
			emptyNode.pointers[j] = nonLeafNode.pointers[i+1];
			nonLeafNode.pointers[i+1].parent = emptyNode;
			nonLeafNode.pointers[i+1] = null;

			//handling number of keys
			nonLeafNode.numKeys--;
			emptyNode.numKeys++;

		}

		//eventually, just removing the splitKey node from the now filled Empty node
		for(int i=1;i<emptyNode.numKeys;i++){
			emptyNode.keys[i-1] = emptyNode.keys[i];
			emptyNode.keys[i]=0;
		}
		emptyNode.numKeys--;


		SplitReturn ret = new SplitReturn(nonLeafNode,emptyNode,splitKey);	
		return ret;
	}

	//recursive method for merging into the parent and going upwards
	public void mergeIntoParent(Node oldNodeWithLeftHalf,Node newNodeWithRightHalf,double key){
		//root case and other cases handled seperately
		//NOTE this rootcase handles both where oldNodeWithLeftHalf is leaf or not
		if(oldNodeWithLeftHalf.isRoot){
			//allocate a new root and put the newLeaf's first key in it
			Node newRoot = new Node(false,true);
			newRoot.keys[0] = key; //as key is the leftmost key in newNodeWithRightHalf
			newRoot.numKeys++;

			//updating pointers
			newRoot.pointers[0] = oldNodeWithLeftHalf;
			newRoot.pointers[1] = newNodeWithRightHalf;

			oldNodeWithLeftHalf.parent = newRoot;
			newNodeWithRightHalf.parent = newRoot;

			//updating the old root
			oldNodeWithLeftHalf.isRoot = false;
			
			//updating the root !
			root = newRoot;
		

		}
		else{
			
			int i=0;
			for(i=0;i<oldNodeWithLeftHalf.parent.numKeys;i++){
				if(key<oldNodeWithLeftHalf.parent.keys[i])
					break;
			}

			//i is the index of where the key should be inserted
			int whereToInsert = i;

			for(i=oldNodeWithLeftHalf.parent.numKeys;i>whereToInsert;i--){
				//first move everything one space ahead
				//and then insert the new key value pair
				//where it is supposed to be inserted
				oldNodeWithLeftHalf.parent.keys[i] = oldNodeWithLeftHalf.parent.keys[i-1];
				//shifting the pointers appropriately
				oldNodeWithLeftHalf.parent.pointers[i+1] = oldNodeWithLeftHalf.parent.pointers[i];
			}

			//The above loop emptied up the spot where we are supposed to insert
			//so now insert
			oldNodeWithLeftHalf.parent.keys[whereToInsert] = key;
			oldNodeWithLeftHalf.parent.pointers[whereToInsert+1] = newNodeWithRightHalf;
			newNodeWithRightHalf.parent = oldNodeWithLeftHalf.parent;


			oldNodeWithLeftHalf.parent.numKeys++;

			

			//overflow condition
			if(oldNodeWithLeftHalf.parent.numKeys>m-1){
				
				
				//split and recurse
				Node parentsRightHalf = new Node(false,false);

				//splitNonLeaf will split the non-leaf according to how B-trees do it,
				//and store the leftHalf in oldNodeWithLeftHalf.parent and right half in parentsRightHalf
				//and return the splitKey as well
			
				

				SplitReturn split = splitNonLeaf(oldNodeWithLeftHalf.parent,parentsRightHalf);
			

				//recurse to merge in granddad
				mergeIntoParent(split.left,split.right,split.splitKey);
			}

			
		}



	}

	//getSearch method uses printSearch method to return the perfectly formatted
	//string for the search operation on a single key
	public String getSearch(double key, Node node){
		
		Node expectation = binSearch(key,node);
		
		boolean isPresent = false;
		int a;
		Vector<String> result=new Vector<String>();
		for( a=0;a<expectation.numKeys;a++){
			if(key == expectation.keys[a]){
				isPresent = true; break;
			}
		}
		if(!isPresent){
			result.add("Null"); 
		}
		else{
		
			ValueNode temp = expectation.values[a];
			while(temp!=null){
				result.add(temp.value);
				temp = temp.next;
			}
		}
		
		 return printSearch(result);

	}
	public String printSearch(Vector<String> result){
		String toPrint= new String();
		int i;
		for(i=0;i<result.size()-1;i++){
			toPrint+=result.get(i);
			toPrint+=",";
		}
		//last element added seperately so that there is no comma after
		//the last element
		toPrint+=result.get(i);
		return toPrint;
		
	}

	//returns a perferctly formatted string for the range search query
	public String rangeSearch(double key1, double key2){

		Node leaf = binSearch(key1,root);
		Node temp = leaf;
		String result = new String();
		int a;
		while(temp!=null){
			for( a=0;a<temp.numKeys;a++){
				if(key1<=temp.keys[a]&&temp.keys[a]<=key2){
					ValueNode valueTemp = temp.values[a];
					while(valueTemp!=null){
						result+="("+temp.keys[a]+","+valueTemp.value+")"+",";
						valueTemp = valueTemp.next;
					}
				}
				
			}
			temp =temp.next;
		}
		//removing the last comma
		if(result.isEmpty()) result="Null";
		else result = result.substring(0, result.length() - 1);

		return result;
		//System.out.println(result);
		

	}

	//####################################################
	//BELOW METHODS WERE USED FOR TESTING ONLY
	//HELPER PRINT AND BFS SEARCH FUNCTIONS BELOW, IGNORE!!
	//####################################################

	//print a node
	void printNode(Node node){
		if(node==null){ System.out.println("null node"); return;}
		System.out.println("numKeys: "+node.numKeys);
		try{
		for(int i=0;i<node.numKeys;i++){
			System.out.println("key: " + node.keys[i] + ", value: " + node.values[i].value);
		}
		}catch(NullPointerException e){}
		
		System.out.println("----------------");
	}

	void printValues(double key){
		Node leaf = binSearch(key,root);
		int i;
		for(i=0;i<leaf.numKeys;i++){
			if(leaf.keys[i]==key)
				break;
		}
		ValueNode temp = leaf.values[i];
		while(temp!=null){
			System.out.println(temp.value);
			temp=temp.next;
		}
	}

	//print the previous and next value of a node
	void printNextPrev(Node node){
		System.out.println("NEXT");
		if(node.next==null){ System.out.println("null next node");}
		else{
			for(int i=0;i<node.next.numKeys;i++){
			System.out.println("key: " + node.next.keys[i] + ", value: " + node.next.values[i]);
			}
		}
		System.out.println("PREV");
		if(node.previous==null){ System.out.println("null next node");}
		else{
			for(int i=0;i<node.previous.numKeys;i++){
			System.out.println("key: " + node.previous.keys[i] + ", value: " + node.previous.values[i]);
			}
		}
	}

	//print the children of a node
	void printChildren(Node node){

		for(int i=0;i<node.numKeys+1;i++){
			System.out.println("child: "+i);
			if(node.pointers[i]!=null)
				printNode(node.pointers[i]);
		}

	}

	//breadth first print the whole tree, only keys
	void bfsPrint(){
		while(!q.isEmpty()){
			System.out.println("dad");
			printNode(q.peek());
			for(int i=0;i<q.peek().numKeys+1;i++){

				if(q.peek().pointers[i]!=null){
					q.add(q.peek().pointers[i]);
					System.out.println("chil's parent");
					printNode(q.peek().pointers[i].parent);
				}
			}
			
			q.remove();
		}
		


	}
	//print the full linked list formed by the leaves
	void printLL(){
		Node temp = root.pointers[0];
		while(!temp.isLeaf){
			temp = temp.pointers[0];

		}
		 while(temp!=null){
		 	printNode(temp);
		 	temp = temp.next;
		 }

	}	

}
