//page used to display all decks created under one category 
import logo from '../assets/learnd_logo.png'
import { useParams, useNavigate} from 'react-router-dom'
import { useEffect, useState, useRef} from 'react'
import axios from 'axios'
import CreateDeckModal from './modal_components/CreateCategoryOrDeckModal'
import useCreateDeckModal from '../hooks/useCreateDeckModal'
import useIsLoggedOut from '../hooks/useIsLoggedOut'
import { set } from 'zod/v4'
import { useQueries, queryOptions } from '@tanstack/react-query'
import ForceGraph2D, {NodeObject, ForceGraphMethods} from 'react-force-graph-2d'
import useEditCategoryModal from '../hooks/useEditCategoryModal'
import EditCategoryModal from './modal_components/EditCategoryModal'
import TestModal from './modal_components/TestModal'
import useTestModal from '../hooks/useTestModal'


type Deck = {
  id: number,
  name: string,
  category: string
}

type CategoryDTO = {
  id: number,
  name: string
  children: CategoryDTO[]
}

export type GraphNode = {
  id: number
  name: string
}

type GraphLink = {
  source: number
  target: number
}

const fetchDecks = async () : Promise<Deck[]> => {
  const response = await axios.get(
    `http://localhost:8080/api/deck/getAllDecksByUser`,
    { withCredentials: true }
  )
  console.log(response)
  return response.data
}

// recurse to flatten categories into nodes + links for react-force-graph
const buildGraph = (categories: CategoryDTO[], parentId: number | null) => {
  let nodes: any[] = []
  let links: any[] = []

  for (const cat of categories) {
    // Add node
    nodes.push({ id: cat.id, name: cat.name })

    // Add link if there's a parent
    if (parentId !== null) {
      links.push({ source: parentId, target: cat.id })
    }

    //recurse, continue graph building with children of current category 
    const { nodes: childNodes, links: childLinks } = buildGraph(
      cat.children || [],
      cat.id
    )

    nodes = [...nodes, ...childNodes]
    links = [...links, ...childLinks]
  }

  return { nodes, links }
}

  
const DeckHome = () => {
  const param = useParams()
  const navigate = useNavigate()
  const isLoggedOutHook = useIsLoggedOut()
  const [menuOpen, setMenuOpen] = useState<boolean>(false)
  const deckModalHook = useCreateDeckModal()
  const [selectedSourceNode, setSelectedSourceNode] = useState<GraphNode | null>(null)
  const editCategoryModalHook = useEditCategoryModal()
  const testModal = useTestModal()


  
  const sortSourceNodesToFront = (nodes : GraphNode[], links : GraphLink[]) => {
    if (nodes.length === 0 || links.length === 0) return
    const idOfNodesWithParents = new Set<number>()
    for (const link of links) {
      idOfNodesWithParents.add(link.target)
    }
    //get the nodes corresonding to those ids in the set 
    const nodesWithParent = nodes.filter((node) => idOfNodesWithParents.has(node.id))


    const remainingNodes: GraphNode[] = []
    for (const node of nodes) {
      if (!idOfNodesWithParents.has(node.id)) {
        remainingNodes.push(node)
      }
    }
    const allNodes = [...nodesWithParent, ...remainingNodes]
    setGraphData({ nodes: allNodes, links: links })
  }


  const getTree = async (): Promise<CategoryDTO[]> => {
    const response = await axios.get(
      `http://localhost:8080/api/category/getTree`,
      { withCredentials: true }
    )
    return response.data
  }
  const [graphData, setGraphData] = useState<{ nodes: any[]; links: any[] }>({
    nodes: [],
    links: [],
  })

  const [decksQuery, treeData]= useQueries({
    queries: [
      {queryKey:["getDecks"], queryFn: fetchDecks},
      {queryKey:["getCategoryTree"], queryFn: getTree}
    ]
  })
  const decks = decksQuery.data ? decksQuery.data : []
  const tree = treeData.data ? treeData.data : []


  
  useEffect(() => {
    const { nodes, links } = buildGraph(tree, null)
    sortSourceNodesToFront(nodes, links)
  }, [tree])


  const deckButtonHandler = (deckName: string, deckId : number) => {
    navigate(`/${param.username}/${deckName}/${deckId}`)
  }

  const handleNodeClick = (node: GraphNode, event: MouseEvent) => {
    console.log("event.detail is: " + event.detail)
    if (event.shiftKey) {
      // Step 1: Select source node
      setSelectedSourceNode(node)
      return
    } 
    else if (event.ctrlKey || event.metaKey) {
      //we want to allow user to rename or delete the category
      setSelectedSourceNode(node)
      editCategoryModalHook.openModal()
      return
    }
    else if (selectedSourceNode && selectedSourceNode.id !== node.id) {
      // Step 2: Click target node -> create link
      setGraphData((prev) => ({
        ...prev,
        links: [
          ...prev.links,
          { source: selectedSourceNode.id, target: node.id },
        ],
      }))
      try{
        axios.patch(`http://localhost:8080/api/category/${node.id}/${selectedSourceNode.id}/update_parent`, {}, {withCredentials: true})
      } catch (error) {
        console.log("couldn't add parent to category")
      }
      setSelectedSourceNode(null) // reset
    }
  }
  const fgRef = useRef<ForceGraphMethods<NodeObject<GraphNode>, GraphLink>>()
  useEffect(() => {
    if (!fgRef.current) return

    fgRef.current.d3Force('link')?.distance(120)
  }, [graphData.links])

  
  
  return (
    <div className="w-screen h-screen bg-[radial-gradient(circle,_#BCA8A8_0%,_#837675_83%,_#847674_100%)]">
      {testModal.isOpen ? (
        <>
          <TestModal isReview={true} />
        </>
      ) : (
        <></>
      )}
      <div className="">
        {deckModalHook.isOpen && (
          <div className="fixed w-screen h-screen flex items-center justify-center z-50">
            <CreateDeckModal />
          </div>
        )}
        {editCategoryModalHook.isOpen ? (
          <div className="fixed w-screen h-screen flex items-center justify-center z-50">
            <EditCategoryModal
              id={selectedSourceNode?.id}
              name={selectedSourceNode?.name}
            />
          </div>
        ) : (
          <></>
        )}
      </div>
      <div className="block flex flex-row">
        <div className="flex w-full h-[100px]">
          <div className="w-[80px] h-[60px] overflow-hidden mt-[30px] ml-[30px]">
            <a onClick={() => navigate(`/${param.username}/deck_home`)} className='cursor-pointer'>
              <img
                src={logo}
                alt="learnd_logo"
                className="w-full h-full object-cover rounded-[55px]"
              />
            </a>
          </div>
        </div>
        <div className="flex justify-center items-center mr-[3vw]">
          {!menuOpen && (
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="w-[2vw] h-[3.5vh]"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="size-6"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                />
              </svg>
            </button>
          )}
          {menuOpen && (
            <div className="absolute right-[3vw] top-[3vh] mt-2 w-48 bg-white rounded-md shadow-lg flex flex-col">
              <div className="flex ml-auto">
                <button
                  onClick={() => {
                    setMenuOpen(!menuOpen)
                  }}
                  className="bg-[#D5627F] mb-[10px]"
                >
                  x
                </button>
              </div>
              <div className="flex flex-row gap-[10px]">
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => navigate(`/${param.username}`)}
                >
                  Profile
                </button>
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => navigate(`/${param.username}/settings`)}
                >
                  Settings
                </button>
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => {
                    try {
                      axios.post(
                        `http://localhost:8080/api/user/logout`,
                        null,
                        { withCredentials: true }
                      )
                      console.log('successful logout')
                      isLoggedOutHook.setTrue()
                      navigate('/')
                    } catch (error) {
                      console.log('log out failed, error is: ' + error)
                    }
                  }}
                >
                  Log out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
      <hr className="border border-t-1 border-black-500 " />
      <div className="block">
        <div className="ml-[50px] mt-[10px] inline-block rounded-[50px] px-[12px] py-[8px] bg-[#B4B483]">
          {param.username}'s deck portal
        </div>
        <button
          onClick={() => {
            testModal.setTrue()
          }}
          className="cursor-pointer"
        >
          Start Review
        </button>
      </div>
      <div className="flex flex-row mt-[20px]">
        <div className="flex flex-col w-1/5 h-full border border-black-500 h-screen">
          <div className="flex flex-row mb-[15px]">
            <input
              className="rounded-[50px] px-[12px] py-[8px] mt-[10px] ml-[10px] border-none w-[175px]"
              placeholder="Search Decks"
            />
            <button
              className="ml-[15px] mt-[10px] px-[12px] py-[8px]"
              onClick={() => {
                deckModalHook.openModal()
              }}
            >
              Create Category/Deck
            </button>
          </div>
          {decks.length > 0 ? (
            <div className="">
              <div className="">
                {decks.slice(0, -1).map((deck: Deck) => (
                  <div className="" key={deck.id}>
                    <button
                      className="py-[10px] px-[10px] my-[10px] deckLabel cursor-pointer w-full"
                      onClick={() => deckButtonHandler(deck.name, deck.id)}
                    >
                      {deck.name}
                    </button>
                    <hr className="border border-t-1 border-black-500" />
                  </div>
                ))}
              </div>
              <button
                className="deckLabel py-[10px] px-[10px] my-[10px] cursor-pointer w-full"
                onClick={() =>
                  deckButtonHandler(
                    decks[decks.length - 1].name,
                    decks[decks.length - 1].id
                  )
                }
              >
                {decks[decks.length - 1].name}
              </button>
            </div>
          ) : (
            <></>
          )}
        </div>
        <div className="w-4/5 h-full border border-black-500 bg-[linear-gradient(to_top,_#847E5E_10%,_#89825A_30%,_#B4B483_100%)] h-screen">
          <ForceGraph2D
            ref={fgRef}
            graphData={graphData}
            nodeCanvasObject={(
              node: NodeObject & GraphNode,
              ctx: CanvasRenderingContext2D,
              globalScale: number
            ) => {
              const label = node.name
              const fontSize = 12 / globalScale

              // draw circle
              ctx.beginPath()
              ctx.arc(node.x ?? 0, node.y ?? 0, 20, 0, 2 * Math.PI, false)
              ctx.fillStyle = '#27c488'
              ctx.fill()
              ctx.strokeStyle = '#1e293b'
              ctx.stroke()

              // add text
              ctx.font = `${fontSize}px Sans-Serif`
              ctx.textAlign = 'center'
              ctx.textBaseline = 'middle'
              ctx.fillStyle = '#000'
              ctx.fillText(label, node.x ?? 0, node.y ?? 0)
            }}
            linkColor={() => 'rgba(0,0,0,0.6)'}
            linkWidth={2}
            linkDirectionalArrowLength={6} // adds arrows
            linkDirectionalArrowRelPos={0.85} // arrow at target node
            onNodeClick={(node, event) => {
              handleNodeClick(node as GraphNode, event as MouseEvent)
            }}
            nodePointerAreaPaint={(
              node: NodeObject & GraphNode,
              color,
              ctx
            ) => {
              // Paint the entire circle into the hidden "hit canvas"
              ctx.fillStyle = color
              ctx.beginPath()
              ctx.arc(node.x!, node.y!, 20, 0, 2 * Math.PI, false)
              ctx.fill()
            }}
          />
        </div>
      </div>
    </div>
  )
}
export default DeckHome